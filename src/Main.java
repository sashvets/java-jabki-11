import model.Library;

import java.io.IOException;

import ui.LibraryUI;

public class Main {
    public static void main(String[] args) throws IOException {
        Library library = new Library(
                "src/storage/books",
                "src/storage/users",
                "src/storage/borrows"
        );
        new LibraryUI(library).start();
    }
}