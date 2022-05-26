package com.example.writetosharedtests;

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class SambaFileHandler {

    public static final String SAMBA_PREFIX = "smb://";
    String sDIRECTORY_PATH = "192.168.0.34/public/";
    String sAUTH_DOMAIN = "";
    String sAUTH_USERNAME = "";
    String sAUTH_PASSWORD = "";

    public static final int ANONYMOUS_CREDENTIALS = 0;
    public static final int NULL_CREDENTIALS = 1;
    public static final int GIVEN_CREDENTIALS = 2;

    private int iCredentials = 0;

    NtlmPasswordAuthentication NtlmAuthentication;


    public void SetCredentials(int _Credentials) {
        iCredentials = _Credentials;
        switch (iCredentials) {
            case ANONYMOUS_CREDENTIALS:
                Log.d("CREDENTIALS Log", "\t" + "Changed credentials to: " + "ANONYMOUS");
                break;
            case NULL_CREDENTIALS:
                Log.d("CREDENTIALS Log", "\t" + "Changed credentials to: " + "NULL");
                break;
            case GIVEN_CREDENTIALS:
                Log.d("CREDENTIALS Log", "\t" + "Changed credentials to: " + "GIVEN CREDENTIALS");
                break;
            default:
                Log.d("CREDENTIALS ERROR", "\t" + "UNKNOWN CREDENTIALS ASSIGNMENT further Credentials will be reset to ANONYMOUS");
                SetCredentials(ANONYMOUS_CREDENTIALS);
                break;
        }
    }

    private void SetAuthentication() {
        switch (iCredentials) {
            case ANONYMOUS_CREDENTIALS:
                NtlmAuthentication = NtlmPasswordAuthentication.ANONYMOUS;
                break;
            case NULL_CREDENTIALS:
                NtlmAuthentication = new NtlmPasswordAuthentication(null, null, null);
                break;
            case GIVEN_CREDENTIALS:
                NtlmAuthentication = new NtlmPasswordAuthentication(sAUTH_DOMAIN, sAUTH_USERNAME, sAUTH_PASSWORD);
                break;
            default:
                Log.d("CREDENTIALS ERROR", "\t" + "UNKNOWN CREDENTIALS ASSIGNMENT further Credentials will be reset to ANONYMOUS");
                SetCredentials(ANONYMOUS_CREDENTIALS);
                NtlmAuthentication = NtlmPasswordAuthentication.ANONYMOUS;
                break;
        }
    }

    public boolean SavetoSharedFolder(byte[] _content, String _fileName, String _folderName) {
        boolean result = false;
        try {
            SetAuthentication();
            String url = SAMBA_PREFIX + sDIRECTORY_PATH + _folderName + "/";
            if (FolderExists(url)) {
                Log.d("File Log", "Directory Found, writing new file...");
                url += _fileName;
                SmbFile file = new SmbFile(url, NtlmAuthentication);
                SmbFileOutputStream out = new SmbFileOutputStream(file);
                out.write(_content);
                out.flush();
                out.close();
                result = true;
                return result;
            } else {
                result = false;
                Log.d("File Log", "Couldn't create Directory please ensure write permission are enabled in host device");
            }
        } catch (UnknownHostException e) {
            result = false;
        } catch (SmbException e) {
            Log.d("File Log", "ERROR 2 : SmbException");
            Log.d("File Log", "Please check credentials");
            result = false;
        } catch (MalformedURLException e) {
            result = false;
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private boolean FolderExists(String _folderPath) throws MalformedURLException, SmbException {
        boolean result = false;
        try {
            SmbFile folder = new SmbFile(_folderPath, NtlmAuthentication);
            Log.d("File Log", "Looking for Directory...");
            if (folder.exists() == false) {
                Log.d("File Log", "Directory not found creating new directory...");
                folder.mkdirs();
            }
            result = true;
        } finally {
            return result;
        }
    }

    public void FindDirectories() throws MalformedURLException {

        SmbFile[] domains = null;
        try {
            try {
                Log.d("DOMAIN Log", "\t" + "Searching for accessible domains");
                domains = (new SmbFile(SAMBA_PREFIX)).listFiles();

            } catch (SmbException e1) {
                Log.d("DOMAIN Log", "\t" + "Couldn't access any domain");
                e1.printStackTrace();
            }
            for (int i = 0; i < domains.length; i++) {
                Log.d("DOMAIN Log", "\t" + "********   DOMAIN: " + domains[i].getName() + " ************ ");
                Log.d("DOMAIN Log", "\t" + "Path = " + domains[i].getPath());
                Log.d("DOMAIN Log", "\t" + "SERVER LISTING FOR " + domains[i].getName());
                SmbFile[] servers = null;
                try {
                    servers = domains[i].listFiles();
                } catch (SmbException e) {
                    e.printStackTrace();
                }
                if (servers != null) {
                    if (servers.length > 0) {
                        for (int j = 0; j < servers.length; j++) {
                            Log.d("SERVER log", "\t" + "------> Server  " + servers[j].getName() + "<------");
                            Log.d("SERVER log", "\t" + "Path = " + servers[j].getPath());
                            listShares(servers[j].getName());
                        }
                    } else
                        listShares(domains[i].getName());
                } else
                    listShares(domains[i].getName());
            }
            Log.d("debug", "\t" + "Scan finished !");
        } finally {

        }
    }

    private void listShares(String name) {
        Log.d("File Log", "\t" + "File Listing for " + name);
        String host;
        try {
            host = name;
            if (host.endsWith("/")) {
                host = host.substring(0, host.length() - 1);
            }

            NbtAddress addrs = NbtAddress.getByName(host);
            SetAuthentication();
            Log.d("File Log", "\t" + "Host adress to try: smb:// " + addrs.getHostAddress());
            SmbFile test = new SmbFile("smb://" + addrs.getHostAddress(), NtlmAuthentication);

            SmbFile[] files = test.listFiles();
            for (SmbFile s : files) {
                Log.d("File Log", "\t" + s.getName());
            }

        } catch (UnknownHostException e) {
            Log.d("File Log", "ERROR 1 : UnknownHostException");
        } catch (SmbException e) {
            Log.d("File Log", "ERROR 2 : SmbException");
            Log.d("File Log", "Please check credentials");
        } catch (MalformedURLException e) {
            Log.d("File Log", "ERROR 3 : MalformedURLException");
        }
    }
}
