package player;

import javazoom.jl.player.Player;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class  mp3Player implements ActionListener {

    JFrame frame;
    JLabel songName;
    JPanel playerPanel, controlPanel, nextPrevPanel;
    Icon iconPlay,iconPause, iconResume, iconStop, iconNext, iconPrev;
    JButton play, pause, resume, stop ,next , prev, select;
    JFileChooser fileChooser;
    FileInputStream fileInputStream;
    BufferedInputStream bufferedInputStream;
    File myFile = null;
    String fileName , filePath;
    long totalLength, pauseLength;
    Player player;
    Thread playThread;
    Thread runnableThread;
    boolean isPlaying = true;


    public mp3Player() {
        initUI();
        addActionEvents();
    }

    public void initUI() {

        songName = new JLabel("",SwingConstants.CENTER);
        select = new JButton("Select MP3");
        playerPanel = new JPanel();
        controlPanel = new JPanel();
        nextPrevPanel = new JPanel();

        iconPlay = new ImageIcon("resources/images/play-button.png");
        iconPause = new ImageIcon("resources/images/pause-button.png");
        iconResume = new ImageIcon("resources/images/resume-button.png");
        iconStop = new ImageIcon("resources/images/stop-button.png");
        iconNext = new ImageIcon("resources/images/next-button.png");
        iconPrev = new ImageIcon("resources/images/prev-button.png");

        play = new JButton(iconPlay);
        pause = new JButton(iconPause);
        resume = new JButton(iconResume);
        stop = new JButton(iconStop);
        next = new JButton(iconNext);
        prev = new JButton(iconPrev);

        nextPrevPanel.setLayout(new GridLayout(1,2));
        nextPrevPanel.add(prev);
        nextPrevPanel.add(next);

        playerPanel.setLayout(new GridLayout(3,1));
        playerPanel.add(select);
        playerPanel.add(songName);
        playerPanel.add(nextPrevPanel);

        controlPanel.setLayout(new GridLayout(1,4));
        controlPanel.add(play);
        controlPanel.add(pause);
        controlPanel.add(resume);
        controlPanel.add(stop);

        play.setBackground(Color.WHITE);
        pause.setBackground(Color.WHITE);
        resume.setBackground(Color.WHITE);
        stop.setBackground(Color.WHITE);

        frame = new JFrame();
        frame.setTitle("Music Player");

        frame.add(playerPanel,BorderLayout.NORTH);
        frame.add(controlPanel,BorderLayout.SOUTH);
        
        frame.setBackground(Color.WHITE);
        frame.setSize(400,200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public synchronized void addActionEvents(){
        select.addActionListener(this);
        play.addActionListener(this);
        pause.addActionListener(this);
        resume.addActionListener(this);
        stop.addActionListener(this);
        next.addActionListener(this);
        prev.addActionListener(this);
    }

    public synchronized void nextSong() {

        String rememberFileName = null;

        File dir = new File("resources/music");
        String[] list = dir.list();
            for (int i = 0; i < Objects.requireNonNull(list).length; i++ ) {
                if(fileName.equals(list[i])){
                    if(i == list.length-1) {
                        rememberFileName = list[0];
                    } else {
                        rememberFileName = list[i+1];
                    }
                    break;
                }
            }

        stopPlayer();
        fileName = rememberFileName;

        String name = "resources/music/" + fileName;
        myFile = new File(name);
    }

    public synchronized void prevSong() {

        String rememberFileName = null;

        File dir = new File("resources/music");
        String[] list = dir.list();

        for (int i = 0; i < Objects.requireNonNull(list).length; i++ ) {
            if(fileName.equals(list[i])){
                if(i == 0) {
                    rememberFileName = list[list.length-1];
                } else {
                    rememberFileName = list[i-1];
                }
                break;
            }
        }

        stopPlayer();
        fileName = rememberFileName;

        String name = "resources/music/" + fileName;
        myFile = new File(name);
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e) {

        if(e.getSource().equals(select)){

            fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("resources/music"));
            fileChooser.setDialogTitle("Select mp3");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("mp3 files","mp3"));
            if(fileChooser.showOpenDialog(select) == JFileChooser.APPROVE_OPTION) {
                myFile = fileChooser.getSelectedFile();
                fileName = myFile.getName();
                filePath = fileChooser.getSelectedFile().getPath();
                songName.setText("File selected: " + fileName);
            }
        }

        if(e.getSource().equals(play)) {

            playPlayer();

        }

        if(e.getSource().equals(next)) {

            nextSong();
            playPlayer();

        }

        if(e.getSource().equals(prev)) {

            prevSong();
            playPlayer();

        }

        if(e.getSource().equals(pause)){

            pausePlayer();

        }

        if(e.getSource().equals(resume)) {

            resumePlayer();

        }

        if(e.getSource().equals(stop)) {

            stopPlayer();

        }
    }

    private void playPlayer() {

        if(fileName != null ) {

            if(playThread!= null && !player.isComplete() ){
                System.out.println("You cant play");
            } else {
                playThread = new Thread(runnablePlay);
                playThread.start();
                songName.setText("Now Playing: " + fileName);
                isPlaying = true;
            }

        } else {
            songName.setText("No files was selected");
        }
    }
    private void pausePlayer() {

        if( fileName != null) {
            try {
                pauseLength = fileInputStream.available();
                player.close();
                isPlaying = false;
            } catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }

    private void resumePlayer() {

        if(fileName != null && !isPlaying ) {
            runnableThread = new Thread(runnableResume);
            runnableThread.start();
            isPlaying = true;
        } else {
            songName.setText("you cant resume playing");
        }
    }

    private void stopPlayer() {

        if(player != null) {
            playThread = null;
            runnableThread = null;
            fileName = null;
            player.close();
            songName.setText("");
            isPlaying = true;
        }
    }

    Runnable runnablePlay = new Runnable() {
        @Override
        public void run() {
            try{
                fileInputStream = new FileInputStream(myFile);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                player = new Player(bufferedInputStream);
                totalLength = fileInputStream.available();
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable runnableResume = new Runnable() {
        @Override
        public void run() {
            try {
                fileInputStream = new FileInputStream(myFile);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                player = new Player(bufferedInputStream);
                fileInputStream.skip(totalLength - pauseLength);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static void main(String[] args) {
        new mp3Player();
    }
}
