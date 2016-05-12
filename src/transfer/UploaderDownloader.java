package transfer;


import java.io.FileOutputStream;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;


public class UploaderDownloader {
	
	public static String containerName = "myContainer";
	public static String fileReference;
	public static String fileDestination;
	public static String blobName;
	
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
	//Take user input of either upload, download, or list 
		public static void blobAction(String filePath, String action) {
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
				   
				 // Create a permissions object.
				    BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

				    // Include public access in the permissions object.
				    containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

				    // Set the permissions on the container.
				    container.uploadPermissions(containerPermissions);
				    
				    
				    //upload the blob if the user enters the upload string
				    if (action.equalsIgnoreCase("upload")) {
				    
				    blob.uploadFromFile(fileReference);
				    
				    }
				    
				    else if (action.equalsIgnoreCase("list")) {
				    	for (ListBlobItem blobItem : container.listBlobs()) {
						       System.out.println(blobItem.getUri());
						   }
				    }
				    else if (action.equalsIgnoreCase("download")) {
				    	//download the blob with the correct filePath into users folder
					    for (ListBlobItem blobItem : container.listBlobs()) {
					        // If the item is a blob, not a virtual directory.
					        if (blobItem instanceof CloudBlob) {
					        	CloudBlob blob1 = (CloudBlob) blobItem;
					        	if (blob1.getUri().toString().equalsIgnoreCase(filePath)) {
					             blob.download(new FileOutputStream(fileDestination + blob1.getName()));
		
					         }
					     }
					    }
				    }
					  
				    else {
				    	System.out.println("Sorry, that command wasn't recognized!"
				    			+ "Please use the upload, download, or list command");
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
		// this should be user inputted
		//what the user wants to name the blob
		//where the file of the blob is located
		blobName = "input2";
		fileReference = "/Users/juliamcarr/Documents/Test/Testinput";
		fileDestination = "/Users/juliamcarr/Documents/Julia/";
		//blobAction("/Users/juliamcarr/Documents/Test/Testinput","upload");
		//blobAction("","list");
		//uploadBlob();
		blobAction("http://juliamcarr.blob.core.windows.net/mycontainer/input2", "download");
		System.out.println("All done! You have uploaded a blob named " + blobName);
		
	}

}
