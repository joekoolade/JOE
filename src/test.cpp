#include <iostream>

#include "ZipArchive.h"

using std::cout;

int main(int argc, char *argv[]) {

	ZipArchive zip = ZipArchive((char *)"/home/joe/baremetal/classpath/lib/glibj.zip");
	cout << "Exiting!";
}
