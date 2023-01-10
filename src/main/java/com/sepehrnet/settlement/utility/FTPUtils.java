package com.sepehrnet.settlement.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FTPUtils {

    private FTPClient ftpClient = null;

    private void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                log.info("FTP SERVER: " + aReply);
            }
        }
    }

    public boolean connect(String server, int port) {
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.info("Operation failed. Server reply code: " + replyCode);
                ftpClient.disconnect();
                return false;
            }
            return true;
        } catch (IOException ex) {
            log.error("Oops! Something wrong happened for connect to the server");
            ex.printStackTrace();
        }
        return false;
    }

    public boolean login(String username, String password) {
        try {
            boolean success = ftpClient.login(username, password);
            showServerReply(ftpClient);
            if (!success) {
                log.info("Could not login to the server");
                ftpClient.disconnect();
                return false;
            } else {
                log.info("LOGGED IN SERVER");
                return true;
            }
        } catch (IOException ex) {
            log.error("Oops! Something wrong happened for login to the server");
            ex.printStackTrace();
        }
        return false;
    }

    public void disconnect() {
        if (this.ftpClient.isConnected()) {
            try {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
            } catch (IOException ex) {
                log.error("Oops! Something wrong happened for disconnect action");
                ex.printStackTrace();
            }
        }
    }

    public boolean uploadFile(String localFileFullName, String remoteFileName, String hostDir) {
        try {
            this.ftpClient.enterLocalPassiveMode();
            //this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            log.info("Start uploading file");
            try (InputStream input = new FileInputStream(localFileFullName)) {
                boolean success = this.ftpClient.storeFile(hostDir + remoteFileName, input);
                if (success) {
                    log.info("File {} is uploaded successfully.", localFileFullName);
                    return true;
                } else {
                    log.info("can not upload file.");
                    return false;
                }
            }
        } catch (IOException e) {
            log.error("Oops! Something wrong happened for upload file");
            e.printStackTrace();
        }
        return false;
    }

    public boolean downloadFile(String remoteFilePath, String localFilePath) {
        try {
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            log.info("Start downloading file");
            try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
                boolean success = this.ftpClient.retrieveFile(remoteFilePath, fos);
                if (success) {
                    log.info("File {} has been downloaded successfully.", remoteFilePath);
                    return true;
                } else {
                    log.info("can not download file.");
                    return false;
                }
            }
        } catch (IOException e) {
            log.error("Oops! Something wrong happened for download file");
            e.printStackTrace();
        }
        return false;
    }

    public String getFileNameWithPattern(String ftpPath, String fileNamePattern) {
        try {
            ftpClient.changeWorkingDirectory(ftpPath);
            ftpClient.enterLocalPassiveMode();
            FTPFile[] files = this.ftpClient.listFiles();
            for (FTPFile file : files) {
                if (file.getType() == FTPFile.FILE_TYPE) {
                    if (file.getName().startsWith(fileNamePattern)) {
                        log.info("File {} found.", file.getName());
                        return file.getName();
                    }
                }
            }
            log.info("File with pattern {} not found.", fileNamePattern);
            return null;
        } catch (IOException e) {
            log.error("Oops! Something wrong happened for check file exist.");
            e.printStackTrace();
        }
        return null;
    }

/*    public boolean checkFileExist(String ftpPath, String fileName) {
        try {
            FTPFile[] files = this.ftpClient.listFiles(ftpPath);
            for (FTPFile file : files) {
                if (file.getType() == FTPFile.FILE_TYPE) {
                    if (file.getName().equals(fileName)) {
                        log.info("File {} found.", fileName);
                        return Boolean.TRUE;
                    }
                }
            }
            log.info("File {} not found.", fileName);
            return Boolean.FALSE;
        } catch (IOException e) {
            log.error("Oops! Something wrong happened for check file exist.");
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }*/
}
