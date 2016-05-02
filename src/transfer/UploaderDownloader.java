package transfer;


import java.io.FileOutputStream;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;


public class UploaderDownloader {
	
	static String containerName;
	static String fileReference;
	static String blobName;
	
	//blob credentials
	public static String storageConnectionString =
		"DefaultEndpointsProtocol=http;" +
				"AccountName=;" +
				"AccountKey=";
	
	public static void storageConnection(String userName, String key) {
		storageConnectionString = "DefaultEndpointsProtocol=http;" + "AccountName=" 
	+ userName + ";" + "AccountKey=" + key;
	}
	
	//Start connection with associated azure account and create a container
	public static void uploadBlob() {
			try
			{
				// Retrieve storage account from connection-string.
				CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);  
			   
				// Create the blob client.
				   CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
				   
				// Get a reference to a container.
				// The container name must be lower case
			   CloudBlobContainer container = blobClient.getContainerReference(containerName.toLowerCase());

			   // Create the container if it does not exist.
			    container.createIfNotExists();

			    // Create or overwrite Blob with name
			    CloudBlockBlob blob = container.getBlockBlobReference(blobName);
			   
			    blob.uploadFromFile(fileReference);
			    
			    for (ListBlobItem blobItem : container.listBlobs()) {
				       System.out.println(blobItem.getUri());
				   }
			    for (ListBlobItem blobItem : container.listBlobs()) {
			        // If the item is a blob, not a virtual directory.
			        if (blobItem instanceof CloudBlob) {
			            // Download the item and save it to a file with the same name.
			             CloudBlob blob1 = (CloudBlob) blobItem;
			             blob.download(new FileOutputStream("/Users/juliamcarr/Documents/Test/" + blob1.getName()));
			         }
			     }

			}
			catch (Exception e)
			{
			    // Output the stack trace.
			    e.printStackTrace();
			}
		
	}
	
	public static void downloadBlobs() {
		try
		{
		    // Retrieve storage account from connection-string.
		   CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		   // Create the blob client.
		   CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

		   // Retrieve reference to a previously created container.
		   CloudBlobContainer container = blobClient.getContainerReference(containerName);

		   for (ListBlobItem blobItem : container.listBlobs()) {
			    // If the item is a blob, not a virtual directory
			    if (blobItem instanceof CloudBlockBlob) {
			        // Download the text
			    	CloudBlockBlob retrievedBlob = (CloudBlockBlob) blobItem;
			    	System.out.println(retrievedBlob.getName());
			    }
			}
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) {
		storageConnection("juliamcarr",
				"SSpOZPJ5PJx+f/ehu58tf8jam+HRZo3Dpq1/+SvFT8mHBOWbXIN25e4lHadRR2Teq0i/JD4909PJNy30BEAfWA==");
		containerName = "JuliasBlob2";
		blobName = "input";
		fileReference = "/Users/juliamcarr/Documents/workspace/CryptoProj2/input.txt";
		
		uploadBlob();
		System.out.println("All done! You have uploaded a blob named " + blobName);
		
	}

}
