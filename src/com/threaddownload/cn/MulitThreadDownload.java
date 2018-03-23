package com.threaddownload.cn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * java���̶߳ϵ�����
 * @author fxy
 * @since 2018/3/23
 */
public class MulitThreadDownload {
	//�߳�����
	private static int threadCount=3;
	//�߳����ؿ� 
	private static long blockSize;
	//ɾ���ϵ��ļ�����
	private static int  runningThread;
	
    public static void main(String[] args) throws Exception {
    	//���ӷ�����
    	String path="http://169.254.93.59:8080/TencentVideo10.7.1441.0.exe";
    	URL url=new URL(path);
    	HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
    	httpURLConnection.setConnectTimeout(5000);
    	httpURLConnection.setRequestMethod("GET");
    	int code=httpURLConnection.getResponseCode();
    	//������Ч
    	if(code==200){
    		//��ȡ��������ȥ���ļ������ڱ����½�һ��ͬ���Ĵ�С���ļ�
    		int size=httpURLConnection.getContentLength();
    		System.out.println("��ǰ�ļ���С"+size);
    		File file=new File("abc.exe");
    		//д��abc.exe�������ļ�����
    		RandomAccessFile randomAccess=new RandomAccessFile(file,"rw");
    		randomAccess.setLength(size);
    		//�ϵ��ļ��������߳���
    		runningThread=threadCount;
            /*//����ȡ���ļ�д���½��ļ�
    		InputStream inputStream = httpURLConnection.getInputStream();
    		int len=0;
    		byte[] by=new byte[1024];
    		while ((len=inputStream.read(by))!=-1) {
    			randomAccess.write(by, 0, len);
			}*/
    		//���߳�����
    		//blockSize��ʼ��
    		blockSize=size/threadCount;
    		for (int i = 1; i <= threadCount; i++) {
    			//�ļ��ֽڿ�ʼλ��
    			long startIndex=(i-1)*blockSize;
    			//�ļ��ֽڽ���λ��
    			long endIndex=i*blockSize-1;
    			if (i==threadCount) {
					endIndex=size-1;
				}
    			new DownloadThread(path, i, startIndex, endIndex).start();
				System.out.println("��ǰ�߳�"+i+"���ش�"+startIndex+"~"+endIndex+"����");
			}
    		
    	}
    	httpURLConnection.disconnect();
	}
    private static class DownloadThread extends Thread{
    	private String path;
		private int threadId;
    	private long startIndex;
    	private long endIndex;
    	public DownloadThread(String path, int threadId, long startIndex, long endIndex) {
			super();
			this.path = path;
			this.threadId = threadId;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
    	@Override
    	public void run() {
    		try {
    			//�ϵ������ļ���С
    			long total=0;
    			//�ϵ����صļ�¼�ļ�
    			File positionfile=new File(threadId+".txt");
    			//�����߳�֮�󿴿���û�л�����ļ�������оͻ���������λ�õ���ʼλ��
    			if(positionfile.exists()&&positionfile.length()>0){
    				FileInputStream fis=new FileInputStream(positionfile);
    				//ʹ�û�����д���ļ�
    				BufferedReader bf=new BufferedReader(new InputStreamReader(fis));
    				String totalDataStr=bf.readLine();
    				int totaldata=Integer.parseInt(totalDataStr);
    				startIndex+=totaldata;
    				endIndex+=totaldata;
    				System.out.println("��ǰ�߳�"+threadId+"���ش�"+startIndex+"~"+endIndex);
    				fis.close();
    			}
				URL url=new URL(path);
				HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
				httpURLConnection.setConnectTimeout(5000);
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setRequestProperty("Ranger", "bytes="+startIndex+"-"+endIndex);
				int code=httpURLConnection.getResponseCode();
				System.out.println("��ǰ�߳�"+threadId+"code="+code);
				//����ȡ���ļ�д���½��ļ�
				InputStream inputStream = httpURLConnection.getInputStream();
				File file=new File("abc.exe");
				RandomAccessFile randomAccess=new RandomAccessFile(file,"rw");
				//������λ�ö�ȡ��seek�������ö�ȡָ���λ��
				randomAccess.seek(startIndex);
				int len=0;
				byte[] by=new byte[1024*1024];
				while ((len=inputStream.read(by))!=-1) {
					RandomAccessFile rm=new RandomAccessFile(positionfile, "rwd");
					randomAccess.write(by, 0, len);
					//��ǰ�߳����ص��ļ���С
					total+=len;
					rm.write(String.valueOf(total).getBytes());
					rm.close();
				}
				randomAccess.close();
				inputStream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    		finally {
    			//ͬ����ǰ�߳�
    			synchronized (this) {
    				//��ʱ�ļ�ɾ��
    				runningThread--;
        			if(runningThread<1){
        				System.out.println("��ǰ�߳�"+threadId+"�������");
        				//ɾ����ʱ��¼�ļ�
        				for (int i = 1; i <= threadCount; i++) {
    						File file=new File(i+".txt");
    						file.delete();
    						System.out.println("��ǰɾ������ʱ�ļ���"+file.getName());
    					}
        			}
    			}
        	}
	}
    }
	
}
