import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class GameServer {
    private List<Socket> clients = new ArrayList<>();
    private int ballX;
    private int ballY;

    public GameServer(int port) {
        ballX = 400; // Initial X position of the ball
        ballY = 300; // Initial Y position of the ball

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clients.add(clientSocket);
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 12345;
        new GameServer(port);
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private DataOutputStream out;
        private DataInputStream in;

        public ClientHandler(Socket socket) {
            clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new DataOutputStream(clientSocket.getOutputStream());
                in = new DataInputStream(clientSocket.getInputStream());
                int width = 800;
                int height = 600;

                while (true) {
                    BufferedImage gameFrame = createGameFrame(width, height);

                    // Send game frame to client
                    sendFrame(gameFrame);

                    // Receive commands from the client
                    String command = in.readUTF();

                    // Process commands from the client
                    if (command.equals("LEFT")) {
                        ballX -= 10;
                    } else if (command.equals("RIGHT")) {
                        ballX += 10;
                    } else if (command.equals("UP")) {
                        ballY -= 10;
                    } else if (command.equals("DOWN")) {
                        ballY += 10;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private BufferedImage createGameFrame(int width, int height) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(new Color(0x00FF00)); // Green background
            g2d.fillRect(0, 0, width, height);

            // Draw the blue ball at the updated position
            g2d.setColor(new Color(0x0000FF)); // Blue
            int ballSize = 50; // Size of the ball
            g2d.fillOval(ballX, ballY, ballSize, ballSize);

            g2d.dispose();
            return image;
        }

        private void sendFrame(BufferedImage frame) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(frame, "png", byteArrayOutputStream);

            byte[] frameData = byteArrayOutputStream.toByteArray();
            out.writeInt(frameData.length);
            out.write(frameData);
            out.flush();
        }
    }
}
