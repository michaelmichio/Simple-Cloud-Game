import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameClient extends JFrame {
    private Socket serverSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedImage gameFrame;

    public GameClient(String serverAddress, int port) {
        try {
            serverSocket = new Socket(serverAddress, port);
            in = new DataInputStream(serverSocket.getInputStream());
            out = new DataOutputStream(serverSocket.getOutputStream());

            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(800, 600);
            setLocationRelativeTo(null);
            setVisible(true);

            // Add a key listener to capture key presses
            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    // Not used in this example
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    int keyCode = e.getKeyCode();
                    // Send commands to the server based on key presses
                    try {
                        if (keyCode == KeyEvent.VK_LEFT) {
                            out.writeUTF("LEFT");
                        } else if (keyCode == KeyEvent.VK_RIGHT) {
                            out.writeUTF("RIGHT");
                        } else if (keyCode == KeyEvent.VK_UP) {
                            out.writeUTF("UP");
                        } else if (keyCode == KeyEvent.VK_DOWN) {
                            out.writeUTF("DOWN");
                        }
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    // Not used in this example
                }
            });

            setFocusable(true);
            requestFocus();

            // Start a separate thread to continuously receive and display game frames
            new Thread(() -> {
                while (true) {
                    try {
                        int frameLength = in.readInt();
                        byte[] frameData = new byte[frameLength];
                        in.readFully(frameData);

                        // Convert the received byte data to a BufferedImage
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(frameData);
                        BufferedImage receivedFrame = ImageIO.read(byteArrayInputStream);

                        if (receivedFrame != null) {
                            gameFrame = receivedFrame;
                            repaint();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        // super.paint(g);
        // Display the received game frame
        g.drawImage(gameFrame, 0, 0, this);
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 12345;
        SwingUtilities.invokeLater(() -> new GameClient(serverAddress, port));
    }
}
