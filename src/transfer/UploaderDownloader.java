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
		public static void blobAction(String x) {
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
				    if (x.equalsIgnoreCase("upload")) {
				    
				    blob.uploadFromFile(fileReference);
				    
				    }
				    
				    else if (x.equalsIgnoreCase("list")) {
				    	for (ListBlobItem blobItem : container.listBlobs()) {
						       System.out.println(blobItem.getUri());
						   }
				    }
				    else if (x.equalsIgnoreCase("download")) {
				    	 //download the blobs that are in the container to a file
					    for (ListBlobItem blobItem : container.listBlobs()) {
					        // If the item is a blob, not a virtual directory.
					        if (blobItem instanceof CloudBlob) {
					            // Download the item and save it to a file with the same name.
					             CloudBlob blob1 = (CloudBlob) blobItem;
					             blob.download(new FileOutputStream(fileDestination + blob1.getName()));
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
		blobName = "Testinput";
		fileReference = "/Users/juliamcarr/Documents/Test/Testinput";
		fileDestination = "/Users/juliamcarr/Documents/Test/";
		blobAction("upload");
		blobAction("list");
		//uploadBlob();
		System.out.println("All done! You have uploaded a blob named " + blobName);
		
	}

}
