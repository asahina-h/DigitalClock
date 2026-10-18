package example;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.io.*;

public class Clock
{
    private static int frameWidth = 240; // 初期の横幅（大きいサイズ）
    private static int frameHeight = 80; // タテのサイズ
    private static final int LARGE_WIDTH = 240; // 大きいサイズの幅
    private static final int SMALL_WIDTH = 170; // 小さいサイズの幅
    private static long lastClickTime = 0; // 最後のクリック時間
    private static final long DOUBLE_CLICK_THRESHOLD = 300; // ダブルクリックのしきい値（ミリ秒）
    private static final String CONFIG_FILE = "config.properties"; // 設定ファイル名

    public static void main(String[] args)
    {
        // フレームの作成
        JFrame frame = new JFrame("Digital Clock");
        frame.setSize(frameWidth, frameHeight); // サイズ：ヨコ、タテ
        frame.setUndecorated(true); // ウィンドウの枠を非表示
        frame.setOpacity(0.7f); // 透過度を設定
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout()); // フレームのレイアウトを設定

        // 日時表示ラベル
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("Meiryo UI", Font.BOLD, 20));
        label.setOpaque(false); //背景の有無(falseは無し)
        label.setForeground(Color.BLACK);

        // 設定ファイルから背景色を読み込む
        Color backgroundColor = loadBackgroundColor();
        frame.getContentPane().setBackground(backgroundColor); // 背景色を設定

        // ラベルをフレームに直接追加
        frame.add(label, BorderLayout.CENTER);

        // 時計と日付の更新を行うタイマー
        Timer timer = new Timer(1000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // 現在時刻と日付を取得
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
                SimpleDateFormat sdfTime_ss = new SimpleDateFormat("ss");
                SimpleDateFormat sdfDateLong = new SimpleDateFormat("yyyy年M月d日(EEE)"); // 長い日付形式
                SimpleDateFormat sdfDateShort = new SimpleDateFormat("M月d日(EEE)"); // 短い日付形式

                String time = sdfTime.format(new Date());
                String ss = sdfTime_ss.format(new Date());

                // 初期設定でframeWidthが大きい場合（250）なら長い形式を、そうでなければ短い形式を使用
                String date = frameWidth == LARGE_WIDTH ? sdfDateLong.format(new Date()) : sdfDateShort.format(new Date());

                // 秒だけ小さくするためにHTMLを使ってフォントサイズを変更
                String timeWithSmallSeconds = "<html><p style=\"text-align:right\">" + date + "<br>" + time + " <font size='4'>"
                        + ss + "</font></p></html>";

                // 時間と日付を改行して表示
                label.setText(timeWithSmallSeconds);
            }
        });
        timer.start();

        // 常に最前面に表示
        frame.setAlwaysOnTop(true);

        // 画面サイズ（スクリーンサイズ）を取得
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // 現在のウィンドウのサイズを取得
        int windowWidth = frame.getWidth();
        int windowHeight = frame.getHeight();

        // 画面の右端と下端を取得し、ウィンドウの位置を右下に設定
        int x = screenSize.width - windowWidth - 10; // 画面の右端 - ウィンドウの幅
        int y = screenSize.height - windowHeight - 50; // 画面の下端 - ウィンドウの高さ（タスクバーを避けるために40px＋ピッタリは気持ち悪いので10pxの余裕）

        // タスクバーが画面下に配置されている事を想定してウィンドウを配置
        frame.setLocation(x, y);

        frame.setVisible(true);

        // マウスでドラッグしてウィンドウを移動
        frame.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime <= DOUBLE_CLICK_THRESHOLD) // ダブルクリックと認識された場合
                {
                    if (frameWidth == LARGE_WIDTH)
                    {
                        // ダブルクリックで横幅を小さくする
                        frameWidth = SMALL_WIDTH;
                    } else
                    {
                        // ダブルクリックで横幅を大きくする(元に戻す)
                        frameWidth = LARGE_WIDTH;
                    }

                    // フレームのサイズを更新
                    frame.setSize(frameWidth, frameHeight);

                    // ウィンドウの右端を基準に位置を調整
                    int newX = screenSize.width - frameWidth - 10; // 画面の右端から左に移動する位置
                    int newY = frame.getLocation().y; // Y位置はそのままにする
                    frame.setLocation(newX, newY); // 新しい位置にウィンドウを配置
                }
                lastClickTime = currentTime; // 最後のクリック時間を更新
            }
        });

        // ドラッグしてウィンドウを移動
        frame.addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                Point currentLocation = frame.getLocation();
                int x = currentLocation.x + e.getX() - frame.getWidth() / 2;
                int y = currentLocation.y + e.getY() - frame.getHeight() / 2;
                frame.setLocation(x, y);
            }
        });

        // 右クリックメニューの作成
        JPopupMenu popupMenu = new JPopupMenu();

        // 背景色変更メニュー
        JMenuItem changeColorItem = new JMenuItem("背景色を変更");
        changeColorItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // 色選択ダイアログを表示
                Color newColor = JColorChooser.showDialog(frame, "背景色を選択", label.getBackground());
                if (newColor != null) // ユーザーが色を選択した場合
                {
                    // ラベルとフレームの背景色を変更
                    frame.getContentPane().setBackground(newColor); // フレームの背景色

                    // 新しい背景色を保存
                    saveBackgroundColor(newColor);
                }
            }
        });
        popupMenu.add(changeColorItem);

        // 閉じるメニュー
        JMenuItem closeItem = new JMenuItem("閉じる");
        closeItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // アプリケーションを終了
                System.exit(0);
            }
        });
        popupMenu.add(closeItem);

        // 右クリックでメニューを表示
        frame.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger()) // 右クリックされた場合
                {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    // 背景色を設定ファイルから読み込む
    private static Color loadBackgroundColor()
    {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE))
        {
            properties.load(input);
            String colorStr = properties.getProperty("background.color", "255,255,255"); // デフォルト色は白
            String[] rgb = colorStr.split(",");
            int r = Integer.parseInt(rgb[0]);
            int g = Integer.parseInt(rgb[1]);
            int b = Integer.parseInt(rgb[2]);
            return new Color(r, g, b);
        }
        catch (IOException e)
        {
            return Color.WHITE; // エラーが発生した場合は白色
        }
    }

    // 背景色を設定ファイルに保存する
    private static void saveBackgroundColor(Color color)
    {
        Properties properties = new Properties();
        properties.setProperty("background.color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
        try (OutputStream output = new FileOutputStream(CONFIG_FILE))
        {
            properties.store(output, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
