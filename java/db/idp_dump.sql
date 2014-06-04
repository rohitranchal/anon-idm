-- MySQL dump 10.13  Distrib 5.6.14, for osx10.7 (x86_64)
--
-- Host: localhost    Database: idp
-- ------------------------------------------------------
-- Server version	5.6.14

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Claim`
--

DROP TABLE IF EXISTS `Claim`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Claim` (
  `ClaimName` varchar(512) NOT NULL,
  `UserName` varchar(256) NOT NULL,
  `UserRandom` text NOT NULL,
  `UserAnonId` text NOT NULL,
  `IssueDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ClaimName`,`UserName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Claim`
--

LOCK TABLES `Claim` WRITE;
/*!40000 ALTER TABLE `Claim` DISABLE KEYS */;
INSERT INTO `Claim` VALUES ('candidate','alice','GRWMibxxgVm2WAFceeMhiQ==','aY0yWPj+g5EUqj/LDXhey2RC/Dc2VaBtE27gRrETb4TZlA==','2014-06-04 16:36:29'),('student','alice','W4fJbWt3aj2OyZyxYFM5zw==','I4sWheXpnGUcQEj8DAEpRxWiI67JYPqd43l4HqDthhL+oQ==','2014-06-04 04:49:51');
/*!40000 ALTER TABLE `Claim` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Claim_Definition`
--

DROP TABLE IF EXISTS `Claim_Definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Claim_Definition` (
  `Name` varchar(512) NOT NULL,
  `Description` varchar(2048) DEFAULT NULL,
  `PrivateKey` text NOT NULL,
  `PublicParams` text NOT NULL,
  `Digest` text NOT NULL,
  `Sig` text NOT NULL,
  `Cert` text NOT NULL,
  `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Claim_Definition`
--

LOCK TABLES `Claim_Definition` WRITE;
/*!40000 ALTER TABLE `Claim_Definition` DISABLE KEYS */;
INSERT INTO `Claim_Definition` VALUES ('candidate','Ph.D. Candidate','u6Ygwv9pKuXzzrdZNlRSliIawljyPnniSQ1skstJ/JMBnQ==','eyJjdXJ2ZSI6ImRIbHdaU0JoTVFwd0lEYzRNVFExTWpRNE5qUTJPVE01TkRJNU5qZ3pOelk1T1RreU1UWXlOVFl6TkRrM09URXpNakkzQ200Z01qVXpOekU0TXpNNU56WXlOemt3TXpVMk1URTJNVE0yTXpNNE1Ua3dNVFF4TWpJMk9Ua3hDbTR3SURReU56Z3hPVEF3TnprS2JqRWdOREk1TkRrMk56STNPUXB1TWlBek1qSXhNakkxTkRjekNtNHpJRFF5T0RZMU56ZzJPRGNLYkNBek1EZ0siLCJnIjoiWk9nN3ZCTWswd3dCdkxYWGYxMjMydkRoQ3pNN0JYSmphbWQ2cG01dXBLTXFhZz09IiwiZzEiOiJTQ0pNRkJTWmhpVXBFTEdBQ0V4UFlpeGtjRkUrNklHWStpeThrempqdTdsOWhBPT0iLCJnMiI6IlA0YjJlK0JaM3FjZU92UzFXZlJ0UDlQQTNycXhEdHdsTmNlYUxTbDQySDFPQnc9PSIsImczIjoiNUMvTEt1M1ZCeUFQNmJNMFQ2QW95SmVvQm5qbGpTMTBkSGEvNzZReTdzL1dhQT09IiwiaDEiOiJHVCtIeUxLb3diV0k3bzc4RDJ6Y0ZzNm8raVExWmNLYmoxcTVGNUEvb0Rra3lRPT0iLCJoMiI6Ikk1TkdhUFQrbW9QMU9UdkxEVG5lUVIydFpTdURPV0l1SERLVVhpRnlGR1VKeHc9PSIsImgzIjoiZzcycmE4MjF1TVUvWmdkRWVaOFp5TWRUUXgyNDgyUGpCbjlRZlpaY2dQYmlMQT09In0=','MbLANdZYKreh83Pi2AJVojQE5sZZtdXwAp9mhyU+9HRFaur5S5Nvj9/HNkz4WhOl+2jZJLgLdCmQkty3GQrOwg==','Xxk116q99YSie/FFSeY69/pA1I8Mu+9XcAHbMa+wzJXAZNxsJbOT20JXx8H+u0zZ2b3zue5ZJla1ZJuawvaj7wa1R+lF6HxxXI8bxNqx3jicWcI1ylmqYxtz70ditf4xPrAAgWImHmntvOT2lbmU3+o/QluIsCpKf7QDlAoOOBs=','MIICXTCCAcagAwIBAgIEUO3+xzANBgkqhkiG9w0BAQUFADBzMQswCQYDVQQGEwJVUzELMAkGA1UECBMCSU4xFzAVBgNVBAcTDldlc3QgTGFmYXlldHRlMRQwEgYDVQQKEwtydWNoaXRoLm9yZzEaMBgGA1UECxMRSWRlbnRpdHkgUHJvdmlkZXIxDDAKBgNVBAMTA0lEUDAeFw0xMzAxMDkyMzM1MzVaFw0xMzA0MDkyMzM1MzVaMHMxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJJTjEXMBUGA1UEBxMOV2VzdCBMYWZheWV0dGUxFDASBgNVBAoTC3J1Y2hpdGgub3JnMRowGAYDVQQLExFJZGVudGl0eSBQcm92aWRlcjEMMAoGA1UEAxMDSURQMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCM9RcnwPVUBvWF/p+ytyBvYG1HBFIbq5IJ6UHHK6GFSj9jHU3IGE6bgVl9m0gZ8i6JYCcgg0wkgLXHo+xfgz/HtgQhj+vOT6DkPWratI7pScoHe4oQNEu9BaY80cj0oFFvETYu0V2/cjzXeT5y1q6BitCEESgwjs3I9XXjjt2Q2wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAANvrtFrJF3YZwsbc0VMPmnUCXZRiNHoboul7YE00MVzckBSqgoeriNaWNWZDrgvVQRWJxYhuZpGOCCnemCybvLF5Y6ZObMLpwGQBdvMycmHkHsbpCULrFXILqTs6rgjGK5lAq3L21Y1gud0FPE6OXcc4rGfOGFBVbUrBEee85EW','2014-06-04 16:35:58'),('student','Student of Purdue University','suINJo0H+Rfhgtgj7CdbivO5rRadiWf6S5TaDQwHjaFUaA==','eyJjdXJ2ZSI6ImRIbHdaU0JoTVFwd0lEWTJOVFkxTURjM05USTBPRGcxTVRJMU5UWTJNalF5T0RnME5UTXhPVFkzTlRreE5qQTROVFF6Q200Z01qazNNVFkxTlRJME5qWTBOalkxTnpNNU1UTTFNREV5T0RjM016YzBPRFUxTXpFNU5qZ3hDbTR3SURReU9UUTVOamN5TXpFS2JqRWdNemMxT0RBNU5qTTRNd3B1TWlBME1qZzJOVGM0TmpnM0NtNHpJRFF5T1RRNU5qY3lNekVLYkNBeU1qUUsiLCJnIjoia21mM3FuaElHNUdORnhBMHNMRjhrVUExTFpJckhTd1Rhd28ybDdWeHArQ3ZPUT09IiwiZzEiOiJpOXQ5NGxIWGs5NWMxZzcyZmVxeXdZaUVicml2RE5oYVM3UHMwV0RRNG5MeVV3PT0iLCJnMiI6Ik1NcXQxTWVGbmpPNExqSGg4MnVBTmRlYjIxc09xM0NERGdZMm9ZLzlzbWdzUnc9PSIsImczIjoiTXh3czB5d0lMZy9HZGJMaTNxeXNWM0drdEZFeEkyWlpQSisvdlRYWTZqL3dwZz09IiwiaDEiOiJWQktSVTM4cGtscTJmSTgzQlF4amI3b0F6MmJlTU4zckNXVWRWMkhEd0M3Rk1RPT0iLCJoMiI6Im9xRWZuOXA5eUhYN3gvYWhoVWQwV2Y1aWFmK2EwQjJQczVOVjhwc1I1R3IwUlE9PSIsImgzIjoiUkYxNnppNW9Rak4yQ256WWxWNGtSM2x6V2VXeEE2STBsVXpTNE12NVl4SmVEdz09In0=','msX1rTsfve59eqV9lPOeypRrlqttwcw0IpYerSZRBvtUIKP2p7J7zXH3Eb6SkbaI+06cN01mvRkHDpaz6DPjyg==','fE00yilQaFShAA24ak99pyvHEupmuEJsoraIBCVtNXK+7j6ggB6h0CZe7o47z6LPQtpyHjbYcm3+K5PkaSxxXdSP9EqJl644vGk4X7HcAqjW4ZIp6PRGtZu0qmreY/uz5PrKl6dqnXOsTJMRwPTpL4FnM6S9wH+9n2G9Lwt5u+U=','MIICXTCCAcagAwIBAgIEUO3+xzANBgkqhkiG9w0BAQUFADBzMQswCQYDVQQGEwJVUzELMAkGA1UECBMCSU4xFzAVBgNVBAcTDldlc3QgTGFmYXlldHRlMRQwEgYDVQQKEwtydWNoaXRoLm9yZzEaMBgGA1UECxMRSWRlbnRpdHkgUHJvdmlkZXIxDDAKBgNVBAMTA0lEUDAeFw0xMzAxMDkyMzM1MzVaFw0xMzA0MDkyMzM1MzVaMHMxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJJTjEXMBUGA1UEBxMOV2VzdCBMYWZheWV0dGUxFDASBgNVBAoTC3J1Y2hpdGgub3JnMRowGAYDVQQLExFJZGVudGl0eSBQcm92aWRlcjEMMAoGA1UEAxMDSURQMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCM9RcnwPVUBvWF/p+ytyBvYG1HBFIbq5IJ6UHHK6GFSj9jHU3IGE6bgVl9m0gZ8i6JYCcgg0wkgLXHo+xfgz/HtgQhj+vOT6DkPWratI7pScoHe4oQNEu9BaY80cj0oFFvETYu0V2/cjzXeT5y1q6BitCEESgwjs3I9XXjjt2Q2wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAANvrtFrJF3YZwsbc0VMPmnUCXZRiNHoboul7YE00MVzckBSqgoeriNaWNWZDrgvVQRWJxYhuZpGOCCnemCybvLF5Y6ZObMLpwGQBdvMycmHkHsbpCULrFXILqTs6rgjGK5lAq3L21Y1gud0FPE6OXcc4rGfOGFBVbUrBEee85EW','2014-06-04 04:45:18');
/*!40000 ALTER TABLE `Claim_Definition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Claim_Definition_Re_Key`
--

DROP TABLE IF EXISTS `Claim_Definition_Re_Key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Claim_Definition_Re_Key` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(512) NOT NULL,
  `ReKeyInfo` text NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Claim_Definition_Re_Key`
--

LOCK TABLES `Claim_Definition_Re_Key` WRITE;
/*!40000 ALTER TABLE `Claim_Definition_Re_Key` DISABLE KEYS */;
/*!40000 ALTER TABLE `Claim_Definition_Re_Key` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `Name` varchar(256) NOT NULL,
  `PubKeyCertificateFpr` varchar(256) NOT NULL,
  `PubKeyCertificate` text NOT NULL,
  `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES ('alice','D3:9A:B3:58:07:E3:F9:FB:72:67:FB:42:CE:4B:F6:45:2C:F1:DD:0E','-----BEGIN CERTIFICATE-----\r\nMIICQTCCAaqgAwIBAgIEUmV0UzANBgkqhkiG9w0BAQUFADBlMQswCQYDVQQGEwJVUzELMAkGA1UE\r\nCBMCSU4xFzAVBgNVBAcTDldlc3QgTGFmYXlldHRlMRQwEgYDVQQKEwtydWNoaXRoLm9yZzEKMAgG\r\nA1UECxMBQTEOMAwGA1UEAxMFQWxpY2UwHhcNMTMxMDIxMTgzNzA3WhcNMjIwMTA3MTgzNzA3WjBl\r\nMQswCQYDVQQGEwJVUzELMAkGA1UECBMCSU4xFzAVBgNVBAcTDldlc3QgTGFmYXlldHRlMRQwEgYD\r\nVQQKEwtydWNoaXRoLm9yZzEKMAgGA1UECxMBQTEOMAwGA1UEAxMFQWxpY2UwgZ8wDQYJKoZIhvcN\r\nAQEBBQADgY0AMIGJAoGBAM0+Dd8U7rbSP5H8OXjKC/WhDEE2etAX3p9AICP8DVBarpWa+FK2X3wN\r\n77HZRS/ncH/g+29MopdBqLIYcB8k5rmyZZN3DNOlfEkuvf48pgdfKxxUdWtK3h8aE2G102GHTYAr\r\n88QfTwyOORETARuqpMeIECCW/OvhJBRsYu2uE0i3AgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAVWaP\r\n2emo0BrjUIk6wrpII7H0gq1v5o7IwfAXO4guW+8H76Eiu6bDcG/sDDewzKWPR08PPhTHy5ITrEVv\r\nS3+5K5/Gc3Y8x+Hv9UyBh5CQYm0vewPHA5m+mB9DySEdbK6xZx7DAeST+kG7Y4yQnhjP7CPTJpQW\r\nGzrYddVzlD5DZ+4=\r\n-----END CERTIFICATE-----','2014-06-04 04:46:25');
/*!40000 ALTER TABLE `User` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-04 16:19:19
