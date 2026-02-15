module dev.answer.material {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;


    opens dev.answer.material to javafx.fxml;
    exports dev.answer.material;
    opens dev.answer.test to javafx.fxml;
    exports dev.answer.test
            ;

}