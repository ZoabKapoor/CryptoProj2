package transfer;


import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;


public class UploaderDownloader {
	
	static String containerName;
	static String fileReference;
	static String blobName;
	
	//blob credentials
	public static final String storageConnectionString =
			"DefaultEndpointsProtocol=http;" +
					"AccountName=juliamcarr;" +
					"AccountKey=SSpOZPJ5PJx+f/ehu58tf8jam+HRZo3Dpq1/+SvFT8mHBOWbXIN25e4lHadRR2Teq0i/JD4909PJNy30BEAfWA==";
	
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

			}
			catch (Exception e)
			{
			    // Output the stack trace.
			    e.printStackTrace();
			}
		
	}
	public static void main(String[] args) {
		containerName = "JuliasBlob2";
		blobName = "output";
		fileReference = "/Users/juliamcarr/Documents/workspace/CryptoProj2/output.txt";
		
		uploadBlob();
		System.out.println("All done! You have uploaded a blob named " + blobName);

	}

}
