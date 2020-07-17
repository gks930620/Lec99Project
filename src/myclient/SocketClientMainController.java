package myclient;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SocketClientMainController implements Initializable {
    public StackPane stackPane;
    public TextField addressField;
    public TextField portField;
    public TextField nicknameField;
    public Button connectionButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	
    	
    	
        }

    

    public void connectAction() throws IOException {
        

        // 화면 이동시 파라미터를 넘기는 방법
        // FXMLLoader로 fxml 파일을 생성한다.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SocketClient.fxml"));
        // 파라미터로 넘길 값 정리
        String address = "192.168.20.4"	;
        int portNumber = 8888;
        String nickname="aa";

        
        // fxml 파일을 로딩할 때 컨트롤러를 생성해서 로딩한다.
        // 이 때 전달하고자 하는 값을 SocketClientController 생성자에 넣어준다.
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
			@Override
			public Object call(Class<?> param) {
				return new SocketClientController(address, portNumber, nickname);
			}
		});
        
        
        
        // 화면 전환
        AnchorPane chatPane = loader.load();

        
        
        
        // 정상적으로 종료하지 않은 경우 클라이언트 종료
        Stage stage = (Stage) stackPane.getScene().getWindow();
        SocketClientController controller = loader.getController();
        stage.setOnCloseRequest(event -> controller.disconnectServer());

        // stackPane에 채팅화면을 올린다.
        stackPane.getChildren().add(chatPane);
        // 화면 이동시 애니메이션
        chatPane.setTranslateY(stackPane.getHeight());  // 시작값을 화면 높이만큼 설정

        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(chatPane.translateYProperty(), 0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(500), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        stackPane.getChildren().get(0).setVisible(false);
    }
}
