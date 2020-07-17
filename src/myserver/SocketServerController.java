package myserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;

public class SocketServerController implements Initializable {
	// 스레드로 병렬 처리를 할 경우 수천 개의 클라이언트가 동시에 연결되면
	// 서버에서 수천 개의 스레드가 생성되기 때문에 서버 성능이 급격히 저하되고,
	// 다운되는 현상이 발생할 수 있다.
	// 클라이언트의 폭증으로 인해 서버의 과도한 스레드 생성을 방지하려면
	// 스레드풀을 사용하는 것이 바람직하다.
	ExecutorService service; // 스레드풀
	public Label statusLabel; // 현재 접속자 수
	// 리스트뷰의 한 아이템이 내용이 길때 줄바꿈된 형태로 보여주기 위해
	// String이 아닌 Text를 사용하였다.
	public ListView<Text> chatView; // 채팅 목록 리스트뷰
	public ToggleButton serverButton; // 서버 시작, 종료 토글 버튼

	// 클라이언트 목록: ArrayList를 사용하지 않고
	// 스레드에 안전하게 하기 위해 Vector를 사용하였다.
	private List<Client> clientList = new Vector<>();
	private List<String> clientGameList = new ArrayList<String>();
	private List<String> clientIdDList = new Vector<String>();
	// 채팅 데이터
	private ObservableList<Text> logList;
	private ServerSocket serverSocket;
	int count = 0;
	int order = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logList = FXCollections.observableArrayList();
		chatView.setItems(logList);

	}

	public void serverAction(ActionEvent actionEvent) {
		if (serverButton.isSelected()) {
			startServer();
		} else {
			stopServer();
		}
	}

	void startServer() {
		// CPU 코어의 수만큼 스레드를 만들도록 한다.
		service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 100,
				Executors.defaultThreadFactory());
		try {
			// TODO 서버 소켓을 생성하여 serverSocket 필드에 대입하기
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(8888));
		} catch (IOException e) {
			e.printStackTrace();
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					logList.add(new Text("[" + currentTime() + "]\n서버 시작"));
					serverButton.setText("종료");
				});
				while (true) {
					try {
						Socket socket = serverSocket.accept(); // 연결 수락
						Platform.runLater(() -> addMessage(
								"[" + currentTime() + " - " + socket.getRemoteSocketAddress() + "]\n연결수락"));
						clientList.add(new Client(socket));

						// 현재 접속자수 업데이트
						Platform.runLater(() -> statusLabel.setText(String.valueOf(clientList.size())));
					} catch (Exception e) {
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}

				}
			}
		};
		// 스레드 풀에서 처리
		service.submit(runnable);
	}

	void stopServer() {
		try {
			Iterator<Client> iterator = clientList.iterator();
			while (iterator.hasNext()) {
				iterator.next().socket.close();
				iterator.remove();
			}
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			if (service != null && !service.isShutdown()) {
				service.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Platform.runLater(() -> {
				addMessage("[" + currentTime() + "]\n서버 종료");
				serverButton.setText("시작");
			});
		}
	}

	/**
	 * 현재 시각을 반환하는 메소드
	 * 
	 * @return 현재시각
	 */
	private String currentTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	/**
	 * 메시지를 채팅 목록에 추가하는 메소드
	 * 
	 * @param msg
	 */
	private void addMessage(String msg) {
		Text text = new Text(msg);
		// 채팅뷰의 너비에 맞게 자동으로 내용을 줄바꿈해주는 바인딩을 설정한다.
		text.wrappingWidthProperty().bind(new DoubleBinding() {
			@Override
			protected double computeValue() {
				return chatView.getPrefWidth();
			}
		});
		logList.add(text);
		chatView.scrollTo(logList.size());
	}

	class Client {
		Socket socket;

		public Client(Socket socket) {
			this.socket = socket;
			receive();
		}

		/**
		 * 클라이언트로부터 데이터 받기
		 */
		private void receive() {

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							byte[] bytes = new byte[256];
							InputStream inputStream = socket.getInputStream();
							// 클라이언트가 비정상 종료를 했을 경우 IOException 발생
							int readByteCount = inputStream.read(bytes);
							// 클라이언트가 정상적으로 Socket의 close()를 호출했을 경우
							if (readByteCount == -1) {
								throw new IOException("클라이언트 종료");
							}
							String data = new String(bytes, 0, readByteCount, StandardCharsets.UTF_8);

							Platform.runLater(() -> {
								addMessage("[" + currentTime() + "]\n" + data);
							});
							if (data.equals("a")) {
								clientList.get(clientList.size() - 1).send("a" + String.valueOf(clientList.size() - 1));

							}

							if ((data.substring(0, 1).equals("3") || data.substring(0, 1).equals("2")
									|| data.substring(0, 1).equals("1"))) {
								count += Integer.parseInt(data.substring(0, 1));
								order++;
								if (count < 31) {
									if (count >= 10) {
										for (Client client : clientList) {
											client.send(String.valueOf(count) + order % clientList.size());
										}
									} else {
										for (Client client : clientList) {
											client.send("0" + String.valueOf(count) + order % clientList.size());
										}
									}
								}else {
									for(Client client:clientList) {
										client.send("끝:"+(order-1) % clientList.size()+"유저의 패배");
									}
								}
							}
						}
					} catch (IOException e) {
						disconnectClient(e.getMessage());
					}
				}
			};
			service.submit(runnable);
		}

		/**
		 * 클라이언트로 데이터 보내기
		 * 
		 * @param msg
		 */
		private void send(String msg) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
						OutputStream outputStream = socket.getOutputStream();
						outputStream.write(bytes);
						outputStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
						disconnectClient(null);
					}
				}
			};
			service.submit(runnable);
		}

		/**
		 * 클라이언트와 통신이 안될 때 현재 클라이언트 제거
		 */
		private void disconnectClient(String errorMsg) {
			try {
				clientList.remove(Client.this);
				Platform.runLater(() -> {
					String msg = "[" + currentTime() + " - " + socket.getRemoteSocketAddress() + "]\n"
							+ (errorMsg != null ? errorMsg : "클라이언트 통신 안됨");
					addMessage(msg);
					statusLabel.setText(String.valueOf(clientList.size()));
				});
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
