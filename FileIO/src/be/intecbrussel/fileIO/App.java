package be.intecbrussel.fileIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class App { 
	private static List<Path> folders = new LinkedList<>();
	
	public static void main(String[] args) {	
		Path sortedPath = Paths.get("C:\\Users\\Cml\\sorted");
		Path unsortedPath = Paths.get("C:\\Users\\Cml\\unsorted");
		Path hiddenPath = Paths.get("C:\\Users\\Cml\\sorted\\hidden");
		Path summaryPath = Paths.get("C:\\Users\\Cml\\sorted\\summary");		

		moveDirectory(unsortedPath, sortedPath);
		sortDirectory(sortedPath,hiddenPath);		
		makeListOfDirectories(sortedPath);
		deleteEmptyDirectory(folders);
		createSummary(summaryPath);
		writeSummaryFile(summaryPath.toString()+"\\summary.txt");
		readSummary(summaryPath.toString()+"\\summary.txt");
		
	}
	
	
	//Method to read the summary text
	private static void readSummary(String path) {
		Path readBufferPath = Paths.get(path);
		try(BufferedReader reader = Files.newBufferedReader(readBufferPath)){
			System.out.println("-----\n content of summary.txt:\n");
			String line;
	         while ((line = reader.readLine()) != null) {
	            System.out.println(line);
	         }
		}
		catch (IOException e) {
			e.printStackTrace();
        }
	}

	// Method to write a summary text via BufferedWriter
	private static void writeSummaryFile(String givenPath) {
		Path bufferPath = Paths.get(givenPath);
		try (BufferedWriter writer = Files.newBufferedWriter(bufferPath)) {
			writer.write("name || readable || writable ||");
			writer.newLine();
			writer.write("-----");
			writer.newLine();
			for (Path outerPath : folders) {
				if (Files.isDirectory(outerPath)) {
					writer.write(outerPath.getFileName().toString() + ":");
					writer.newLine();
				}
				for (Path path : Files.list(outerPath).collect(Collectors.toList())) {
					if (Files.exists(path)) {
						writer.write(path.getFileName().toString() + "\t|");
						if (Files.isReadable(path)) {
							writer.write("| readable |");
						} else {
							writer.write("| not readable |");
						}

						if (Files.isWritable(path)) {
							writer.write("| writable ||");
						} else {
							writer.write("| not writable||");
						}
						writer.newLine();
						writer.write("-----");
						writer.newLine();
					}
				}
			}
				System.out.println("summary.txt is successfully created");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method to make List of Directories
	private static void makeListOfDirectories(Path sortedPath) {
		// Creating the list of folders in the directory
		try {
			if (Files.exists(sortedPath)) {
				folders = Files.list(sortedPath).collect(Collectors.toList());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// Method to Create Summary Path
	private static void createSummary(Path summaryPath) {
		// Create Summary Directory
		try {
			if (Files.notExists(summaryPath)) {
				Files.createDirectory(summaryPath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method to Delete empty directories
	private static void deleteEmptyDirectory(List<Path> folders) {
		List<Path> tempFolders = new LinkedList<>();
		for (Path pathParent : folders) {
			try {
				for (Path path : Files.list(pathParent).collect(Collectors.toList())) {
					if (Files.isDirectory(path) && isDirEmpty(path)) {
						Files.deleteIfExists(path);
						tempFolders.add(path);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (Files.isDirectory(pathParent) && isDirEmpty(pathParent)) {
					Files.deleteIfExists(pathParent);
					tempFolders.add(pathParent);
				}
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
		folders.removeAll(tempFolders);
	}

	// Method to move the Directory
	private static void moveDirectory(Path unsortedPath, Path sortedPath) {
		// Copying unsorted folder to sorted directory to make changes
		try {
			Files.walkFileTree(unsortedPath, new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					return copy(file);
				}

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return copy(dir);
				}

				private FileVisitResult copy(Path fileOrDir) throws IOException {
					Files.copy(fileOrDir, sortedPath.resolve(unsortedPath.relativize(fileOrDir)));
					return FileVisitResult.CONTINUE;
				}

			});
			System.out.println("Unsorted directory moved to Sorted");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Method to sort directory
	public static void sortDirectory(Path sortedPath, Path hiddenPath) {
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String extension = getFileExtensions(file);
				// System.out.println(extension);
				Path newPath = Paths.get("C:\\Users\\Cml\\sorted\\" + extension);

				if (Files.notExists(newPath) && !Files.isHidden(file)) {
					Files.createDirectory(newPath);

				}
				if (!Files.isHidden(file)) {
					Path newDest = Paths.get(newPath + "\\" + file.getFileName());

					Files.move(file, newDest, StandardCopyOption.REPLACE_EXISTING);
				}

				else {
					if (Files.notExists(hiddenPath)) {
						Files.createDirectory(hiddenPath);
					}
					Path newHiddenPath = Paths.get(hiddenPath + "\\" + file.getFileName());
					Files.move(file, newHiddenPath, StandardCopyOption.REPLACE_EXISTING);
				}
				return FileVisitResult.CONTINUE;

			}
		};

		try {
			Files.walkFileTree(sortedPath, visitor);
			System.out.println("Directory is sorted successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method for getting file extensions
	private static String getFileExtensions(Path file) {
		String name = file.getFileName().toString();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return name.substring(lastIndexOf + 1);
	}

	// Method to check if a directory is empty
	private static boolean isDirEmpty(final Path directory) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		}
	}
}
