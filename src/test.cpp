#include <iostream>

#include "ZipArchive.h"

using std::cout;

int main(int argc, char *argv[]) {

	ZipArchive zip = ZipArchive("/home/joe/m/classpath/lib/glibj.zip");
	cout << "Exiting!";
}
