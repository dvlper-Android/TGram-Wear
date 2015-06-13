package org.tgramwear;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.widget.ImageView;

import com.google.android.gms.wearable.Asset;
import org.tgramwear.ContactE;
import org.tgramwear.MainSTW;
import org.tgramwear.SendMessageTW;

import org.telegram.android.ImageReceiver;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Components.AvatarDrawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by dvlper.android on 15/05/2015.
 */

public class DvlperUtils {


/*----START SERIALIZERS----*/
    /**
     * Giving set, stores the set as object into memory
     * @param contactsCopy
     * @return  0 ALL OK
     *          1 EMPTY contactsCopy
     *          2 FILE_NOT_FOUND
     *          3 I/O EXCEPTION
     */
    private static int serializeContacts(Set contactsCopy){
        int ret = 0;
        if(contactsCopy.size()!=0) {
            try {
                FileOutputStream outputStream = new FileOutputStream(checkFileExist("sortedContactsFile",true));
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(contactsCopy);
                oos.close();
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ret = 2;
            } catch (IOException e) {
                ret = 3;
            }
        } else {
            ret = 1;
        }
        return ret;
    }

    /**
     * Writes to memory bitmap
     * @param fileName
     * @param bm
     * @return  0 ALL OK
     *          1 EMPTY contactsCopy
     *          2 FILE_NOT_FOUND
     *          3 I/O EXCEPTION
     */
    private static int serializeBitmap(String fileName, Bitmap bm){
        int ret = 0;
        if(bm.getWidth() != 0) {
            checkFileExist(fileName, true);
            FileOutputStream fileOutStream = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                fileOutStream = ApplicationLoader.applicationContext.getApplicationContext().openFileOutput(fileName, 0x0000);
                bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                fileOutStream.write(b);
                fileOutStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ret = 2;
            } catch (IOException e) {
                e.printStackTrace();
                ret = 3;
            }
        } else {
            ret = 1;
        }
        return ret;
    }

    /**
     * Retreive from memory a pre-serialized Set<ContactE> object
     * @return Set<ContactE> UserContacts array(id, name, surname, phone)
     */
    public static Set<ContactE> DeSerializeContacts(){
        Set contactsCopy = new TreeSet<ContactE>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(checkFileExist("sortedContactsFile",false));
            ObjectInputStream ois = new ObjectInputStream(fis);
            contactsCopy = (TreeSet<ContactE>) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (OptionalDataException e) {
            e.printStackTrace();
            return null;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return contactsCopy;
    }
/*----END SERIALIZERS----*/

/*----START CONVERTERS----*/
    /**
     * Convert drawable to bitmap
     * @param drawable
     * @return bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        // We ask for the bounds if they have been set as they would be most
        // correct, then we check we are  > 0
        final int width = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().height() : drawable.getIntrinsicHeight();

        // Now we check we are > 0
        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Create Asset from bitmap and compress to PNG
     *
     * @param bitmap
     * @return Asset
     */
    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        try {
            byteStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Asset.createFromBytes(byteStream.toByteArray());
    }

    /**
     * Converts the URI received from parameter to Bitmap
     * @param photoData
     * @return
     */
    public static Bitmap fromUriToBitmap(String photoData) {
        Bitmap bmp = null;
        // Creates an asset file descriptor for the thumbnail file.
        AssetFileDescriptor afd = null;
        // try-catch block for file not found
        try {
            // Creates a holder for the URI.
            Uri thumbUri;
            // If Android 3.0 or later
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // Sets the URI from the incoming PHOTO_THUMBNAIL_URI
                thumbUri = Uri.parse(photoData);
            } else {
                // Prior to Android 3.0, constructs a photo Uri using _ID
				/*
				 * Creates a contact URI from the Contacts content URI incoming
				 * photoData (_ID)
				 */
                final Uri contactUri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_URI, photoData);
				/*
				 * Creates a photo URI by appending the content URI of
				 * Contacts.Photo.
				 */
                thumbUri = Uri.withAppendedPath(contactUri,
                        ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            }

			/*
			 * Retrieves an AssetFileDescriptor object for the thumbnail URI
			 * using ContentResolver.openAssetFileDescriptor
			 */
            afd = ApplicationLoader.applicationContext.getContentResolver().openAssetFileDescriptor(thumbUri,
                    "r");
			/*
			 * Gets a file descriptor from the asset file descriptor. This
			 * object can be used across processes.
			 */
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            // Decode the photo file and return the result as a Bitmap
            // If the file descriptor is valid
            if (fileDescriptor != null) {
                // Decodes the bitmap
                // bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor,
                // null, null);
                bmp = BitmapFactory.decodeStream(new FileInputStream(
                        fileDescriptor));
            }
            // If the file isn't found
        } catch (FileNotFoundException e) {
			/*
			 * Handle file not found errors
			 */
        }
        // In all cases, close the asset file descriptor
        finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                }
            }
        }
        return bmp;
    }

/*----END CONVERTERS----*/

/*----START SAVERS----*/
    /**
     * Open a Drawable File from memory and loads it to the ImageView
     * @param fileName
     * @param iv ImageView
     */
    public static void setAvatarToImageView(String fileName, ImageView iv){
        File filePath = ApplicationLoader.applicationContext.getFileStreamPath(fileName);
        Drawable d;

        if(filePath.length() != 0){
            d = Drawable.createFromPath(filePath.getAbsolutePath());
            iv.setImageDrawable(d);
        }
    }

    /***
     * Extracts avatar from dc, convert it to compressed Bitmap and creates a .PNG file with the user.id as name
     * @paramDialogCell dc
     */
    public static int saveUserAvatar(DialogCell dc){
        ImageReceiver c = dc.getavatarImage();
        String fileName = dc.getUserId() + ".PNG";
        return serializeBitmap(fileName, c.getBitmap());
    }

    /**
     * Givin an user extracts his photo_big, convert to bitmap and stores a .PNG to
     * memory with the user.id as name
     * @param user
     */
    public static int saveUserAvatar(TLRPC.User user){
        String fileName = user.id + ".PNG";
        AvatarDrawable ad = new AvatarDrawable(user);
        Bitmap bmp= drawableToBitmap(ad.getCurrent());
        return serializeBitmap(fileName,bmp);
    }

    /**
     * Givin an user extracts his photo_big, convert to bitmap and stores a .PNG to
     * memory with the user.id as name
     * @parambitmap bmp, String fileName
     */
    public static int saveUserAvatar(Bitmap bmp, String fileName){
        return serializeBitmap(fileName,bmp);
    }

    /**
     * Check if the file exist and depending on deleteExistent delete de file if exist
     * @param fileName
     */
    public static File checkFileExist(String fileName, Boolean deleteExistent){
        Boolean exist = true;
        File file = new File(ApplicationLoader.applicationContext.getFilesDir(),fileName);

        if(!file.exists())
            exist = false;

        if (deleteExistent && exist) {
            file.delete();
        } else {
            file.setWritable(true);
            file.setReadable(true);
        }
        return file;
    }
/*----END SAVERS----*/

/*----START TELEGRAM CONTACTS----*/
    /**
     * Sends a message
     * @param message String
     * @return always 0
     */
    public static int sendMessage(String message, long user_id){
        ContactE contact;
        Iterator it;
        TLRPC.UserProfilePhoto upf = null;
        SendMessageTW smtw = null;

        if(user_id != 0)
            smtw = new SendMessageTW(message, user_id);
        else
            return 1;

        if(smtw == null || !smtw.getSended()){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     * Retreive all the Telegram contacts and creates a TreeSet to store them.
     * After that create a file named sortedContactsFile into memory to serialize the TreeSet and store it.
     * Now also tryes to save the Avatar from Marta and Aida to test
     * @param usersDict
     */
    public static void dumpContactsFromTelegramToFileObject(HashMap<Integer, TLRPC.User> usersDict){
        Set contactsCopy = new TreeSet<ContactE>();
        int i = 0;

        for (Map.Entry<Integer, TLRPC.User> entry : usersDict.entrySet()) {
            contactsCopy.add(new ContactE(entry.getValue().id, entry.getValue().first_name, entry.getValue().phone, entry.getValue().id));
        }
        serializeContacts(contactsCopy);
    }


    /**
     * Returns image URI from the name received by parameter from phone contacts
     */
    public static String getPhoneContactPhoto(String name) {
        String photo = null;
        String contacts;
        boolean trobat = false;

        Cursor info = ApplicationLoader.applicationContext
                .getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI, null,
                        null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        while (info.moveToNext() && !trobat) {
            contacts = info.getString(info.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if (contacts.equalsIgnoreCase(name)) {
                photo = info.getString(info.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                trobat = true;
            }
        }
        info.close();

        return photo;
    }
/*----END TELEGRAM CONTACTS----*/

}