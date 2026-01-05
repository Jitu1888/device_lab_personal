package com.games24x7.actuatordevices.controller;

import java.io.File;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;


import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
public class s3Buckket {

	public  void upload(String folderPath) {
		
		try {
			AWSCredentials credentials = new BasicAWSCredentials("","");
			 AmazonS3 s3client =
			 AmazonS3ClientBuilder.standard()
			   .withCredentials(new AWSStaticCredentialsProvider(credentials))
			   .withRegion(Regions.AP_SOUTH_1)
			   .build();
			 
			   TransferManager transferManager = new TransferManager(s3client);
			   MultipleFileUpload upload=  transferManager.uploadDirectory("g24x7.artifacts/QA_Automation_Code_Coverage", folderPath,
					   new File(System.getenv("HOME")+"/device_lab/actuatordevices/Logs/"+folderPath), true);
			   upload.isDone();
			   System.out.println(folderPath +  " upload done");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
	}
}
