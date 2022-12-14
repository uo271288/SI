package aimax.osm.gui.fx.applications;

import aimax.osm.data.DataResource;
import aimax.osm.gui.fx.viewer.MapPaneCtrl;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Locale;

// VM options (Java>8): --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml

/**
 * Shows a map of the city of Ulm with pan and zoom functionality (mouse drag, mouse wheel, arrow keys,
 * plus and minus keys). Set markers with Mouse-Left and remove them with Mouse-Right. Symbols can be changed in size
 * by zooming with Ctrl-Button pressed. Information about special map entities can be obtained by Mouse-Middle.
 *
 * @author Ruediger Lunde
 */
public class InteractiveMapViewerOsmApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // indicates progress when reading large maps (for testing only)
        // Logger.getLogger("aimax.osm").setLevel(Level.FINEST);
        // Logger.getLogger("").getHandlers()[0].setLevel(Level.FINE);

        Locale.setDefault(Locale.US);

        StackPane mapPane = new StackPane();
        MapPaneCtrl mapPaneCtrl = new MapPaneCtrl(mapPane);
        mapPaneCtrl.loadMap(DataResource.getUlmFileResource());

        BorderPane root = new BorderPane();
        root.setCenter(mapPane);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Interactive Map Viewer OSM App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
