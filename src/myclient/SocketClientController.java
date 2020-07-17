package myclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;

public class SocketClientController implements Initializable {

	private String address;
	private int portNumber;
	private String nickname;
	private Socket socket;
	IntegerProperty countProperty=new SimpleIntegerProperty();
	@FXML
	AnchorPane anchorPane;
	@FXML
	ToggleGroup r1;
	@FXML
	Button quitButton;
	@FXML
	TextField textField;

	@FXML
	TextField joinedPeopel;
	int myClientOrder;
	@FXML
	Button sendButton;
	
	String a="";
	boolean sendB=true;

	public SocketClientController(String address, int portNumber, String nickname) {
		this.address = address;
		this.portNumber = portNumber;
		this.nickname = nickname;

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sendButton.setDisable(true);
		startClient();
		
		// 선택안됐으면 보내기버튼 비활성화하고싶은데..
		r1.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
			if((Toggle)observable.getValue().getToggleGroup().getSelectedToggle()==null){
				sendButton.setDisable(true);
			}
				
			}
		});
		
		
		
		//여기다 그 데이터가 0이면 된다.
//		sendButton.disableProperty().bind(Bindings.createBooleanBinding(new Callable<Boolean>() {
//			@Override
//			public Boolean call() throws Exception {
//				return !(textField.getText().equals("0")&&joinedPeopel.getText().equals(a.substring(2)));     //return이 false라 이용가능해진다  
//			}
//		}, textField.lengthProperty(),joinedPeopel.lengthProperty()));
	

	}

	void receive() {
		while (true) {
			try {
				byte[] bytes = new byte[512];
				InputStream inputStream = socket.getInputStream();
				// 서버가 비정상적으로 종료했을 경우 IOException 발생
				int readByteCount = inputStream.read(bytes);
				// 서버가 정상적으로 Socket의 close()를 호출했을 경우
				if (readByteCount == -1) {
					throw new IOException();
				}
				String data = new String(bytes, 0, readByteCount, StandardCharsets.UTF_8);
				a=data;
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (data.substring(0, 1).equals("a")) {
							joinedPeopel.setText(data.substring(1));
							if(data.substring(1).equals("0")) {
								sendButton.setDisable(false);
								sendB=false;
							}
						}
						if(data.substring(0,1).equals("끝")) {
							textField.setText(data);
							sendButton.setDisable(true);
							
						}
						if (data.substring(0, 2).equals("01") || data.substring(0, 2).equals("02")
								|| data.substring(0, 2).equals("03") || data.substring(0, 2).equals("04")
								|| data.substring(0, 2).equals("05") || data.substring(0, 2).equals("06")
								|| data.substring(0, 2).equals("07") || data.substring(0, 2).equals("08")
								|| data.substring(0, 2).equals("09") || data.substring(0, 2).equals("10")
								|| data.substring(0, 2).equals("11") || data.substring(0, 2).equals("12")
								|| data.substring(0, 2).equals("13") || data.substring(0, 2).equals("14")
								|| data.substring(0, 2).equals("15") || data.substring(0, 2).equals("16")
								|| data.substring(0, 2).equals("17") || data.substring(0, 2).equals("18")
								|| data.substring(0, 2).equals("19") || data.substring(0, 2).equals("20")
								|| data.substring(0, 2).equals("21") || data.substring(0, 2).equals("22")
								|| data.substring(0, 2).equals("23") || data.substring(0, 2).equals("24")
								|| data.substring(0, 2).equals("25") || data.substring(0, 2).equals("26")
								|| data.substring(0, 2).equals("27") || data.substring(0, 2).equals("28")
								|| data.substring(0, 2).equals("29") || data.substring(0, 2).equals("30")
								|| data.substring(0, 2).equals("31")) {
							textField.setText(data.substring(0, 2));
							if (data.substring(2).equals(joinedPeopel.getText())) {
								sendButton.setDisable(false);
							} else {
								sendButton.setDisable(true);
							}
						}

					}
				});
			} catch (IOException e) {
				disconnectServer();
				break;
			}
		}
	}

	@FXML
	public void sendNumberAction() {
		send(((RadioButton) r1.getSelectedToggle()).getText());
	}

	// 보내는내용 nickname대신 tg에 달린 숫자보내기
	void send(String msg) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
					OutputStream outputStream = socket.getOutputStream();
					outputStream.write(bytes);
					outputStream.flush();
					System.out.println(msg);
					Platform.runLater(() -> {

					});
				} catch (IOException e) {
					e.printStackTrace();
					disconnectServer();
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	void startClient() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(address, portNumber)); // 서버에연결
					Platform.runLater(() -> {
						// addMessage("[연결됨]");
						textField.setText("0"); // 소켓연결되면서 설정
						send("a"); // a는 소켓연결이라는 의미
					});
				} catch (IOException e) {
					// 종료누르면 이 예외상황에 해당하게된다.
					disconnectServer();
				}
				receive();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	@FXML
	public void quitAction() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		StackPane root = (StackPane) anchorPane.getScene().getRoot();

		// 화면 이동시 애니메이션
		anchorPane.setTranslateY(0); // 시작값을 화면 높이만큼 설정

		Timeline timeline = new Timeline();
		KeyValue keyValue = new KeyValue(anchorPane.translateYProperty(), root.getHeight());
		KeyFrame keyFrame = new KeyFrame(Duration.millis(1000), event -> {
			// stackPane에 채팅화면을 올린다.
			root.getChildren().remove(anchorPane);
		}, keyValue);
		timeline.getKeyFrames().add(keyFrame);
		timeline.play();
		root.getChildren().get(0).setVisible(true);
	}

	void disconnectServer() {
		if (!socket.isClosed()) {
			quitAction();
		}
	}
}
