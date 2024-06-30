package frc.utils.applicationsutils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

public class FileCreator {

    public static File createFile(Path pathName) {
        return createFile(new File(pathName.toString()));
    }

    public static File createFile(Path parent, String name, String type) {
        return createFile(new File(parent.toString()), name, type);
    }

    public static File createFile(File parent, String name, String type) {
        return createFile(new File(parent,name + "." + type));
    }

    public static File createFile(String name, String type) {
        return createFile(new File(name + "." + type));
    }

    public static File createFile(File file) {
        try {
            file.createNewFile();
            return file;
        } catch (Exception exception) {
            System.out.println("Unable To create File");
            exception.printStackTrace();
            return null;
        }
    }

    public static void writeToTextFile(File file, String text) {
        try {
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(text);
            myWriter.close();
        } catch (Exception exception) {
            System.out.println("An error occurred.");
            exception.printStackTrace();
        }
    }

    public static void clearTextFile(File file) {
        try {
            FileWriter myWriter = new FileWriter(file);
            myWriter.flush();
            myWriter.close();
        } catch (Exception exception) {
            System.out.println("An error occurred.");
            exception.printStackTrace();
        }
    }

    public static void openFile(File file) {
        try
        {
            if(!Desktop.isDesktopSupported())
            {
                System.out.println("not supported");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if(file.exists()) {
                desktop.open(file);
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static File createPrintingTextFile(Path parent, String name, String text) {
        File parentFile = parent.toFile();
        if (!parentFile.exists()) {
            parentFile.mkdir();//makes folder
        }
        File file = createFile(parent,name, "txt");
        clearTextFile(file);
        writeToTextFile(file, text);
        return file;
    }
}
