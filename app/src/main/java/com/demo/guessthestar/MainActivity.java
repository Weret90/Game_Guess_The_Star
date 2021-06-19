package com.demo.guessthestar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    private String url = "https://cinewest.ru/amerikanskie-aktery-top-50-gollivudskih-muzhchin/";

    private ArrayList<String> names;
    private ArrayList<String> imagesUrls;
    private ArrayList<Button> buttons;

    private int numberOfQuestion;
    private int numberOfRightAnswer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageViewStar);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        buttons = new ArrayList<>();
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);
        imagesUrls = new ArrayList<>();
        names = new ArrayList<>();
        getContent();
        playGame();
    }

    public void onClickAnswer(View view) {
        Button button = (Button) view;
        String tag = button.getTag().toString();
        if (Integer.parseInt(tag) == numberOfRightAnswer) {
            Toast.makeText(this, "Правильно", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "НЕВЕРНО!!! Это был " + names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
        }
        playGame();
    }


    private void playGame() {
        generateQuestion();
        DownloadImageTask task = new DownloadImageTask();
        try {
            Bitmap bitmap = task.execute(imagesUrls.get(numberOfQuestion)).get();
            if (bitmap != null) {
                String rightAnswer = names.get(numberOfQuestion);
                imageView.setImageBitmap(bitmap);
                for (int i = 0; i < buttons.size(); i++) {
                    if (i == numberOfRightAnswer) {
                        buttons.get(i).setText(rightAnswer);
                    } else {
                        while (true) {
                            String wrongAnswer = names.get(generateWrongAnswer());
                            if (!wrongAnswer.equals(rightAnswer)) {
                                buttons.get(i).setText(wrongAnswer);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generateQuestion() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberOfRightAnswer = (int) (Math.random() * buttons.size());
    }

    private int generateWrongAnswer() {
        return (int) (Math.random() * names.size());
    }

    private void getContent() {
        DownloadPageCode task = new DownloadPageCode();
        try {
            String pageCode = task.execute(url).get();
            String start = "<p>Представляем список американских актеров мужчин: голливудских мачо, комедийных актеров, заслуженных ветеранов экрана и их более молодых коллег.</p>";
            String finish = "<h3>НОВОСТИ ПАРТНЁРОВ</h3>";
            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(pageCode);
            String splitContent = "";
            while (matcher.find()) {
                splitContent = matcher.group(1);
            }
            Pattern imagePattern = Pattern.compile("<p><a href=\"http(.*?)\" data-wpel");
            Pattern namePatter = Pattern.compile("\"text-align: center;\">(.*?)</h2>");
            Matcher imageMatcher = imagePattern.matcher(splitContent);
            Matcher nameMatcher = namePatter.matcher(splitContent);
            while (imageMatcher.find()) {
                imagesUrls.add("https" + imageMatcher.group(1));
            }

            while (nameMatcher.find()) {
                names.add(nameMatcher.group(1));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class DownloadPageCode extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            URL url = null;
            HttpURLConnection httpURLConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return result.toString();
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return null;
        }
    }

}