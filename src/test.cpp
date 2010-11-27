#include <iostream>
#include <stdio.h>

#include "ZipArchive.h"
#include "ZipFile.h"
#include "ClassFile.h"

using std::cout;

int main(int argc, char *argv[]) {
	ZipFile zfile;

	ZipArchive zip = ZipArchive((char *)"/home/joe/m/classpath/lib/glibj.zip");
	printf("Finished with the archive!\n");
	zip.iterator();
	while(zip.hasNext()) {
		zfile = zip.next();
		printf("name: %s\n", zfile.name.c_str());
		try {
			ClassFile c(zfile);
		} catch(int i) {
			printf("caught: %s ---> %d\n", zfile.name.c_str(), i);
		}
	}
}
