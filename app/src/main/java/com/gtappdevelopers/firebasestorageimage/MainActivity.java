package com.gtappdevelopers.firebasestorageimage;

import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.stream.IntStream;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.util.Date;


public class MainActivity extends AppCompatActivity {

    //vaiables for imageview,edittext,button, bitmap and qrencoder.
    private ImageView qrCodeIV;
    private EditText dataEdt;
    private Button generateQrBtn;
    private ImageView Login;
    private Button Upd;
    private TextView showww;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializing all variables.

        qrCodeIV = findViewById(R.id.idIVQrcode);
        dataEdt = findViewById(R.id.idEdt);
        showww=findViewById(R.id.showw);
        generateQrBtn = findViewById(R.id.idBtnGenerateQR);
        Login = findViewById(R.id.Login);
        Upd = findViewById(R.id.UpdateK);


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login.setVisibility(View.INVISIBLE);
                qrCodeIV.setVisibility(View.VISIBLE);
                generateQrBtn.setVisibility(View.VISIBLE);
                showww.setVisibility(View.VISIBLE);
                dataEdt.setVisibility(View.VISIBLE);

                Upd.setVisibility(View.VISIBLE);



                generateQrBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    //intializing onclick listner for button.
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(dataEdt.getText().toString())) {
                            //if the edittext inputs are empty then execute this method showing a toast message.
                            Toast.makeText(MainActivity.this, "Enter some text to generate QR Code", Toast.LENGTH_SHORT).show();
                        } else {

                            //below line is for getting the windowmanager service.
                            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                            //initializing a variable for default display.
                            Display display = manager.getDefaultDisplay();
                            //creating a variable for point which is to be displayed in QR Code.
                            Point point = new Point();
                            display.getSize(point);
                            //getting width and height of a point
                            int width = point.x;
                            int height = point.y;
                            //generating dimension from width and height.
                            int dimen = width < height ? width : height;
                            dimen = dimen * 3 / 4;

                            String sign=stringify(sign_msg(dataEdt.getText().toString()));

                        qrgEncoder = new QRGEncoder(sign, null, QRGContents.Type.TEXT, dimen);
                        showww.setText(sign);


                            //setting this dimensions inside our qr code encoder to generate our qr code.
                            //qrgEncoder = new QRGEncoder(dataEdt.getText().toString(), null, QRGContents.Type.TEXT, dimen);

                            try {
                                //getting our qrcode in the form of bitmap.
                                bitmap = qrgEncoder.encodeAsBitmap();
                                // the bitmap is set inside our image view using .setimagebitmap method.
                                qrCodeIV.setImageBitmap(bitmap);
                            } catch (WriterException e) {
                                //this method is called for exception handling.
                                Log.e("Tag", e.toString());
                            }
                        }
                    }
                });
                Upd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText UpdName = findViewById(R.id.UpdateName);
                        EditText UpdKeyNo = findViewById(R.id.UpdateKeyNo);
                        EditText UpdHour = findViewById(R.id.UpdateHours);

                        Date date = new Date();
                        long epochTime = date.getTime() / 1000L;

                        String public_key = stringify(get_public_key());

                        Button fin = findViewById(R.id.FinUpd);
                        UpdName.setVisibility(View.VISIBLE);
                        UpdKeyNo.setVisibility(View.VISIBLE);
                        UpdHour.setVisibility(View.VISIBLE);
                        fin.setVisibility(View.VISIBLE);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("Users");

                        String KeyNo = UpdKeyNo.getText().toString();

                        String noofHours = UpdHour.getText().toString();

                        myRef.setValue("Hello, World!");
                        myRef.child("User1").child("KeyNo").setValue(KeyNo);
                        myRef.child("User1").child("noofHours").setValue(noofHours);
                        myRef.child("User1").child("Time").setValue(epochTime);
                        myRef.child("User1").child("PublicKey").setValue(public_key);
                    }


                });
            }
        });
    }
    public byte[] sign_msg(String msg) {
        KeyStore.PrivateKeyEntry key;
                try {
                    key = getKeyPair();

                    Signature signObj;
                    if (key != null) {
                        signObj = Signature.getInstance("SHA256withECDSA");
                        signObj.initSign(key.getPrivateKey());
                    signObj.update(msg.getBytes(StandardCharsets.UTF_8));
                    byte[] res = signObj.sign();

//            Log.i("Sign","Sign = " + res.size + " " + res.contentToString())
                    byte[] r = int_2_arr(IntStream.range(4,4 + res[3] - 1).map(i -> res[i]).toArray());
//                    byte[] r = res.(4, 4 + res[3].toInt())
                    byte[] s = int_2_arr(IntStream.range(4 + res[3] + 2,4+res[3] + 2 + res[4 + res[3]]).map(i -> res[i]).toArray());
//                    byte[] s = res.copyOfRange(4 + res[3].toInt() + 2, 4 + res[3].toInt() + 2 + res[4 + res[3].toInt() + 1].toInt())

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    if (r[0] == 0) {
                        outputStream.write(int_2_arr(IntStream.range(1,r.length - 1).map(i -> r[i]).toArray()));
                    } else {
                        outputStream.write(r);
                    }

                    if (s[0] == 0) {
                        outputStream.write(int_2_arr(IntStream.range(1,s.length - 1).map(i -> s[i]).toArray()));
                    } else {
                        outputStream.write(s);
                    }

                    byte[] sign_value = outputStream.toByteArray();

//            Log.i("Sign","Sign Value = " + sign_value.size + " " + sign_value.contentToString())
                    return sign_value;
                }else{
                        return null;
                    }
    } catch (Exception e) {
                    Log.i("Sign", e.toString());
                    return null;
                }
    }

    private byte[] get_public_key() {
        KeyStore.PrivateKeyEntry key;
        try{
            key = getKeyPair();
            byte[] res = key.getCertificate().getPublicKey().getEncoded();
            byte[] pubKey = int_2_arr(IntStream.range(91-64,90).map(i -> res[i]).toArray());
            return pubKey;
        }catch (Exception e){
            Log.i("Sign",e.toString());
            return null;
        }
    }

    private KeyStore.PrivateKeyEntry getKeyPair() {
        KeyStore keyStore;
        KeyStore.Entry res;
        KeyPairGenerator generator;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            res = keyStore.getEntry("OUR_KEYPAIR", null);

            if (res == null) {
                generator = KeyPairGenerator.getInstance("EC", "AndroidKeyStore");
                generator.initialize(new KeyGenParameterSpec.Builder("OUR_KEYPAIR", KeyProperties.PURPOSE_SIGN).setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1")).setDigests(KeyProperties.DIGEST_SHA256).setKeySize(256).build());
                generator.generateKeyPair();
                return (KeyStore.PrivateKeyEntry) keyStore.getEntry("OUR_KEYPAIR", null);
            } else {
                return (KeyStore.PrivateKeyEntry) res;
            }
        } catch (Exception e) {
            Log.i("Sign", e.toString());
            return null;
        }
    }

    private byte[] int_2_arr(int[] int_array){
        int i;
        byte[] byte_array = new byte[int_array.length];

        for(i=0;i<int_array.length;i++){
            byte_array[i] = (byte) (int_array[i] & 0x00ff);
        }

        return byte_array;
    }

    private String stringify(byte[] byte_arr){
        String str = "";

        for(int i = 0; i<byte_arr.length;i++){

            str += byte_arr[i] + " ";
//            Log.i("Sign",String.valueOf(byte_arr[i]));
        }

        return str;
    }

//    private fun byte_2_hex(byteArray: ByteArray): String {
//        var st=""
//        for (b in byteArray) {
//            st += String.format("%02X", b)
//        }
//
//        return st
//    }
}

