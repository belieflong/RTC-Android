package org.webrtc;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FileWriteUtil {

    private static File pcmFile = null;
    private static DataOutputStream dos;

    private static File h264File = null;
    private static DataOutputStream dosH;

    /**
     * 写采集后的PCM文件 需要转成short数组
     * @param byteBuffer ByteBuffer
     */
    public void writePCM(ByteBuffer byteBuffer) {
        try {
            if (pcmFile == null) {
                pcmFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/record_test.pcm");
                if (pcmFile.exists()) {
                    pcmFile.delete();
                    Logger.e("delete local file");
                }
                pcmFile.createNewFile();
                Logger.e("create local file");
            }
        } catch (IOException e) {
            Logger.e("Can not create");
            throw new IllegalStateException("Can not create " + pcmFile.toString());
        }

        try {
            if (dos == null) {
                OutputStream os = new FileOutputStream(pcmFile);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                dos = new DataOutputStream(bos);
            }

            //byte[] saveByte = new byte[byteBuffer.remaining()]; //存在疑问与Encoder中写H264做对比
            //byteBuffer.get(saveByte, 0, saveByte.length);

            if (dos != null) {
                byte[] byteSave = byteBuffer.array();
                short[] shortSave = new short[byteSave.length / 2];
                //byte数组转换为short数组(同时设置byte数组->顺序为小端存储，但short数组跟随系统->大端)[16 大 1 48000 -> 可行]
                ByteBuffer.wrap(byteSave).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortSave); //写入short数据，为什么是大端 (order)
                for (int i = 0; i < shortSave.length; i++) {
                    dos.writeShort(shortSave[i]);
                }
                //dos.write(byteBuffer.array());
            }
        } catch (Exception e) {
            Logger.e("write file fail " + e.getMessage());
        }
    }

    /**
     * 写编码后的H264文件 需要转成byte数组
     * @param byteBuffer
     */
    public static void writeH264(ByteBuffer byteBuffer) {
        if (byteBuffer == null)
            return;
        try {
            if (h264File == null) {
                h264File = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/h264_file.h264");

                if (h264File.exists()) {
                    h264File.delete();
                    Logger.e("delete local file");

                }
                h264File.createNewFile();
                Logger.e("create local file");

            }

        } catch (IOException e) {
            Logger.e("Can not create");
            throw new IllegalStateException("Can not create " + h264File.toString());
        }

        try {
            if (dosH == null) {
                OutputStream os = new FileOutputStream(h264File);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                dosH = new DataOutputStream(bos);
            }

            if (dosH != null) {
                byte[] saveByte = new byte[byteBuffer.remaining()];
                byteBuffer.get(saveByte, 0, saveByte.length);
                dosH.write(saveByte);
                dosH.flush();
            }

        } catch (Exception e) {
            Logger.e("write file fail " + e.getMessage());
        }
    }

    public static void close() {
        try {
            if (dos != null){
                dos.close();
            }
            if (dosH != null) {
                dosH.close();
            }
            Logger.e("write file close");
        } catch (IOException e) {
            Logger.e("close stream fail");
        }
    }

    /**
     * 获取日期文件名称
     * @return 年-月-日-时-分
     */
    public static String getDateFileName() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        String subyear = String.valueOf(year).substring(2, 4);
        String month = String.format("%02d", (calendar.get(Calendar.MONTH) + 1));
        String day = String.format("%02d", (calendar.get(Calendar.DATE)));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String minute = String.format("%02d", (calendar.get(Calendar.MINUTE)));
        String second = String.format("%02d", (calendar.get(Calendar.SECOND)));

        String date = "" + subyear + "-" + month + "-" + day + "-" + hour + "-" + minute + second;
        return date;
    }

    /**
     * 把文件列表名称转换成字符串数组
     * @param flist 文件列表
     * @return fileName[]
     */
    public static String[] FileNameToStr(List<File> flist) {
        ArrayList<String> listStr = new ArrayList<String>();
        int i;
        for (i = 0; i < flist.size(); i++) {
            String nameString = flist.get(i).getName();
            listStr.add(nameString);
        }
        return listStr.toArray(new String[listStr.size()]);
    }

    /**
     // write file -> HardwareVideoEncoder.java
     01. init
     private boolean isWriteEncodedFile = true;

     02. write
     EncodedImage encodedImage = builder.createEncodedImage();
     if (isWriteEncodedFile) FileWriteUtil.writeH264(encodedImage.buffer);
     callback.onEncodedFrame(encodedImage, new CodecSpecificInfo());

     03. close
     if (isWriteEncodedFile) FileWriteUtil.close();
     */
}
