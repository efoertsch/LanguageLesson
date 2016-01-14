package com.fisincorporated.languagetutorial.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

//http://stackoverflow.com/questions/2520305/java-io-to-copy-one-file-to-another
//http://www.jondev.net/articles/Unzipping_Files_with_Android_%28Programmatically%29
public class FileUtil {
	public static final String TAG = "FileUtil";

	public FileUtil() {
		// TODO Auto-generated constructor stub
	}

	public static void copyFileToFile(final File src, final File dest)
			throws IOException {
		copyInputStreamToFile(new FileInputStream(src), dest);
		dest.setLastModified(src.lastModified());
	}

	public static void copyInputStreamToFile(final InputStream in,
			final File dest) throws IOException {
		copyInputStreamToOutputStream(in, new FileOutputStream(dest));
	}

	public static void copyInputStreamToOutputStream(final InputStream in,
			final OutputStream out) throws IOException {
		copyInputStreamToOutputStream(in, out, 1024);
	}

	public static void copyInputStreamToOutputStream(final InputStream in,
			final OutputStream out, int buffersize) throws IOException {
		try {
			try {
				final byte[] buffer = new byte[buffersize];
				int n;
				while ((n = in.read(buffer)) != -1)
					out.write(buffer, 0, n);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	// zipTargetDirectory must already be created and make sure have write access
	// to it
	// using *** Apache commons ZipArchiveInputStream ***
	public static void unzipArchive(InputStream zipInputStream,
			String zipTargetDirectory) throws IOException {
		ZipArchiveInputStream zipIn = new ZipArchiveInputStream(zipInputStream);
		unzipZipFile(zipTargetDirectory, zipIn);
	}

	// zipTargetDirectory must already be created and make sure have write access
	// to it
	// using *** Apache commons ZipArchiveInputStream ***
	public static void unzipArchive(String zipFile, String zipTargetDirectory)
			throws IOException {
		ZipArchiveInputStream zipIn = new ZipArchiveInputStream(
				new FileInputStream(zipFile));
		unzipZipFile(zipTargetDirectory, zipIn);
	}

	// using *** Apache commons ZipArchiveInputStream ***
	private static void unzipZipFile(String zipTargetDirectory,
			ZipArchiveInputStream zipIn) throws IOException,
			UnsupportedEncodingException, FileNotFoundException {
		ArchiveEntry zipEntry = null;
		int size;
		byte[] buffer = new byte[4096];
		while ((zipEntry = zipIn.getNextZipEntry()) != null) {
			Log.i("Decompress", "Unzipping " + zipEntry.getName());
			if (zipEntry.isDirectory()) {
				checkForDirectory(zipTargetDirectory, zipEntry.getName());
			} else {
				BufferedOutputStream bufferOut = new BufferedOutputStream(
						new FileOutputStream(zipTargetDirectory + File.separator
								+ zipEntry.getName()), buffer.length);
				while ((size = zipIn.read(buffer, 0, buffer.length)) != -1) {
					bufferOut.write(buffer, 0, size);
				}
				bufferOut.flush();
				bufferOut.close();
				Log.i(TAG, "Entry:" + zipEntry.getName() + " closed.");
			}
		}
		zipIn.close();
	}

	// zipTargetDirectory must already be created and make sure have write access
	// to it
	// using *** Java ZipInputStream ***
	public static void unzip(String zipFile, String zipTargetDirectory)
			throws IOException {
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zipEntry = null;
		int size;
		byte[] buffer = new byte[4096];
		while ((zipEntry = zipIn.getNextEntry()) != null) {
			Log.i("Decompress", "Unzipping " + zipEntry.getName());
			if (zipEntry.isDirectory()) {
				checkForDirectory(zipTargetDirectory, zipEntry.getName());
			} else {
				BufferedOutputStream bufferOut = new BufferedOutputStream(
						new FileOutputStream(zipTargetDirectory + File.separator
								+ zipEntry.getName()), buffer.length);
				while ((size = zipIn.read(buffer, 0, buffer.length)) != -1) {
					bufferOut.write(buffer, 0, size);
				}
				bufferOut.flush();
				bufferOut.close();
				Log.i(TAG, "Entry:" + zipEntry.getName() + " closed.");
			}
		}
		zipIn.close();
	}

	private static void checkForDirectory(String targetDirectory, String dir)
			throws IOException {
		File f = new File(targetDirectory + dir);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}

	public static void showZipFileEntries(String zipFilename) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilename);
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();
				String name = zipEntry.getName();
				long size = zipEntry.getSize();
				long compressedSize = zipEntry.getCompressedSize();
				Log.e(TAG, String.format(
						"name: %-20s | size: %6d | compressed size: %6d\n", name,
						size, compressedSize));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (zipFile != null)
				try {
					zipFile.close();
				} catch (IOException e) {
				}
		}
	}

	// Keep just in case
	@SuppressWarnings("finally")
	public static String readAssetsText(Context context, String fileName) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		AssetManager assetManager = context.getResources().getAssets();
		InputStream inputStream = null;

		try {
			inputStream = assetManager.open(fileName);
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(context,
					"Oops " + fileName + " file is missing " + e.toString(),
					Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(context,
					"Oops. Error reading " + fileName + " file" + e.toString(),
					Toast.LENGTH_LONG).show();
			;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					;
				}
			}
			return sb.toString();
		}

	}

	// Returns external application directory free space
	// free space
	// http://stackoverflow.com/questions/2941552/how-can-i-check-how-much-free-space-an-sd-card-mounted-on-an-android-device-has
	/**
	 * These methods are designed to get available space in external storage of
	 * android. It contains methods which provide you the available space in
	 * different units e.g bytes, KB, MB, GB. OR you can get the number of
	 * available blocks on external storage.
	 * 
	 */

	// *********
	// Variables
	/**
	 * Number of bytes in one KB = 2<sup>10</sup>
	 */
	public final static long SIZE_KB = 1024L;

	/**
	 * Number of bytes in one MB = 2<sup>20</sup>
	 */
	public final static long SIZE_MB = SIZE_KB * SIZE_KB;

	/**
	 * Number of bytes in one GB = 2<sup>30</sup>
	 */
	public final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	// ********
	// Methods

	/**
	 * @return Number of bytes available on external storage
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static long getExternalAvailableSpaceInBytes() {
		long availableSpace = -1L;
		try {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				availableSpace = stat.getAvailableBlocksLong()
						* stat.getBlockSizeLong();
			} else {
				availableSpace = (long) stat.getAvailableBlocks()
						* (long) stat.getBlockSize();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return availableSpace;
	}

	/**
	 * @return Number of kilo bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInKB() {
		return getExternalAvailableSpaceInBytes() / SIZE_KB;
	}

	/**
	 * @return Number of Mega bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInMB() {
		return getExternalAvailableSpaceInBytes() / SIZE_MB;
	}

	/**
	 * @return gega bytes of bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInGB() {
		return getExternalAvailableSpaceInBytes() / SIZE_GB;
	}

	/**
	 * @return Total number of available blocks on external storage
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static long getExternalStorageAvailableBlocks() {
		long availableBlocks = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			availableBlocks = stat.getAvailableBlocksLong();
		} else {
			availableBlocks = (long) stat.getAvailableBlocks();
		}
		return availableBlocks;
	}

	// And the same for free internal storage
	
	// http://stackoverflow.com/questions/4595334/get-free-space-on-internal-memory
	/**
	 * @return Total number of available blocks on internal storage
	 * 
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static long getInternalAvailableSpaceInBytes(Context context) {
		long availableSpace = -1L;
		try {
			StatFs stat = new StatFs(context.getFilesDir().getAbsolutePath());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				availableSpace =  stat.getAvailableBlocksLong()
						*   stat.getBlockSizeLong();
			} else {
				availableSpace = (long) stat.getAvailableBlocks()
						* (long) stat.getBlockSize();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return availableSpace;
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static long getInternalStorageAvailableBlocks(Context context) {
		long availableBlocks = -1L;
		try {
			StatFs stat = new StatFs(context.getFilesDir().getAbsolutePath());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				 availableBlocks = stat.getAvailableBlocksLong();
			} else {
				 availableBlocks = (long) stat.getAvailableBlocks();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  availableBlocks;
	}
}
