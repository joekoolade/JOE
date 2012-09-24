#include <windows.h>
#include <tchar.h>
#include <stdio.h>
#include "../../zip.h"
#include "../../unzip.h"


// This program shows how to modify an existing zipfile --
// add to it, remove from it


ZRESULT RemoveFileFromZip(const TCHAR *zipfn, const TCHAR *zename);
ZRESULT AddFileToZip(const TCHAR *zipfn, const TCHAR *zename, const TCHAR *zefn);
// AddFileToZip: eg. zipfn="c:\\archive.zip", zefn="c:\\docs\\file.txt", zename="file.txt"
// If the zipfile already contained something called zename (case-insensitive), it is removed.
// These two functions are defined below.



void main()
{ printf("This program shows how to modify an existing zipfile -- add to it, remove from it.\n");
 
  // First we'll create some small files
  const char *s; FILE *f;
  CreateDirectory("\\z",0);
  s="a contents\r\n"; f=fopen("\\z\\a.txt","wt"); fputs(s,f); fclose(f);
  s="b stuff\r\n";    f=fopen("\\z\\b.txt","wt"); fputs(s,f); fclose(f);
  s="c something\r\n";f=fopen("\\z\\c.txt","wt"); fputs(s,f); fclose(f);
  s="c fresh\r\n";    f=fopen("\\z\\c2.txt","wt");fputs(s,f); fclose(f);
  s="d up\r\n";       f=fopen("\\z\\d.txt","wt"); fputs(s,f); fclose(f);
  
  // and create a zip file to be working with
  printf("Creating '\\z\\modify.zip' with zna.txt, znb.txt, znc.txt and a folder...\n");
  HZIP hz = CreateZip("\\z\\modify.zip",0);
  ZipAdd(hz,"zna.txt","\\z\\a.txt");
  ZipAdd(hz,"znb.txt","\\z\\b.txt");
  ZipAdd(hz,"znc.txt","\\z\\c.txt");
  ZipAddFolder(hz,"znsub");
  CloseZip(hz);
  
  printf("Adding znsub\\znd.txt to the zip file...\n");
  AddFileToZip("\\z\\modify.zip","znsub\\znd.txt","\\z\\d.txt");

  printf("Removing znb.txt from the zip file...\n");
  RemoveFileFromZip("\\z\\modify.zip","znb.txt");

  printf("Updating znc.txt in the zip file...\n");
  AddFileToZip("\\z\\modify.zip","znc.txt","\\z\\c2.txt");

  return;
}


// AddFileToZip: adds a file to a zip, possibly replacing what was there before
// zipfn ="c:\\archive.zip"             (the fn of the zip file) 
// zefn  ="c:\\my documents\\file.txt"  (the fn of the file to be added)
// zename="file.txt"                    (the name that zefn will take inside the zip)
// If zefn is empty, we just delete zename from the zip archive.
// The way it works is that we create a temporary zipfile, and copy the original
// contents into the new one (with the appropriate addition or substitution)
// and then remove the old one and rename the new one. NB. we are case-insensitive.
ZRESULT RemoveFileFromZip(const TCHAR *zipfn, const TCHAR *zename)
{ return AddFileToZip(zipfn,zename,0);
}
ZRESULT AddFileToZip(const TCHAR *zipfn, const TCHAR *zename, const TCHAR *zefn)
{ if (GetFileAttributes(zipfn)==0xFFFFFFFF || (zefn!=0 && GetFileAttributes(zefn)==0xFFFFFFFF)) return ZR_NOFILE;
  // Expected size of the new zip will be the size of the old zip plus the size of the new file
  HANDLE hf=CreateFile(zipfn,GENERIC_READ,FILE_SHARE_READ,0,OPEN_EXISTING,0,0); if (hf==INVALID_HANDLE_VALUE) return ZR_NOFILE; DWORD size=GetFileSize(hf,0); CloseHandle(hf);
  if (zefn!=0) {hf=CreateFile(zefn,GENERIC_READ,FILE_SHARE_READ,0,OPEN_EXISTING,0,0); if (hf==INVALID_HANDLE_VALUE) return ZR_NOFILE; size+=GetFileSize(hf,0); CloseHandle(hf);}
  size*=2; // just to be on the safe side.
  //
  HZIP hzsrc=OpenZip(zipfn,0); if (hzsrc==0) return ZR_READ;
  HZIP hzdst=CreateZip(0,size,0); if (hzdst==0) {CloseZip(hzsrc); return ZR_WRITE;}
  // hzdst is created in the system pagefile
  // Now go through the old zip, unzipping each item into a memory buffer, and adding it to the new one
  char *buf=0; unsigned int bufsize=0; // we'll unzip each item into this memory buffer
  ZIPENTRY ze; ZRESULT zr=GetZipItem(hzsrc,-1,&ze); int numitems=ze.index; if (zr!=ZR_OK) {CloseZip(hzsrc); CloseZip(hzdst); return zr;}
  for (int i=0; i<numitems; i++)
  { zr=GetZipItem(hzsrc,i,&ze); if (zr!=ZR_OK) {CloseZip(hzsrc); CloseZip(hzdst); return zr;}
    if (stricmp(ze.name,zename)==0) continue; // don't copy over the old version of the file we're changing
    if (ze.attr&FILE_ATTRIBUTE_DIRECTORY) {zr=ZipAddFolder(hzdst,ze.name); if (zr!=ZR_OK) {CloseZip(hzsrc); CloseZip(hzdst); return zr;} continue;}
    if (ze.unc_size>(long)bufsize) {if (buf!=0) delete[] buf; bufsize=ze.unc_size*2; buf=new char[bufsize];}
    zr=UnzipItem(hzsrc,i,buf,bufsize); if (zr!=ZR_OK) {CloseZip(hzsrc); CloseZip(hzdst); return zr;}
    zr=ZipAdd(hzdst,ze.name,buf,bufsize); if (zr!=ZR_OK) {CloseZip(hzsrc); CloseZip(hzdst); return zr;}
  }
  delete[] buf;
  // Now add the new file
  if (zefn!=0) {zr=ZipAdd(hzdst,zename,zefn); if (zr!=ZR_OK) {CloseZip(hzsrc); CloseZip(hzdst); return zr;}}
  zr=CloseZip(hzsrc); if (zr!=ZR_OK) {CloseZip(hzdst); return zr;}
  //
  // The new file has been put into pagefile memory. Let's store it to disk, overwriting the original zip
  zr=ZipGetMemory(hzdst,(void**)&buf,&size); if (zr!=ZR_OK) {CloseZip(hzdst); return zr;}
  hf=CreateFile(zipfn,GENERIC_WRITE,0,0,CREATE_ALWAYS,FILE_ATTRIBUTE_NORMAL,0); if (hf==INVALID_HANDLE_VALUE) {CloseZip(hzdst); return ZR_WRITE;}
  DWORD writ; WriteFile(hf,buf,size,&writ,0); CloseHandle(hf);
  zr=CloseZip(hzdst); if (zr!=ZR_OK) return zr;
  return ZR_OK;
}

