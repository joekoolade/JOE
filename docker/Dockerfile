FROM centos:7

RUN yum update -y
RUN yum install -y bash-completion bzip2 gcc gcc-c++ git make ncurses-devel patch
RUN yum install -y rsync tar unzip wget curl which diffutils python3 python3-pip
RUN yum install -y flex bison
RUN pip3 install gdown
RUN gdown -O /usr/local/openjdk-7u75-b13-linux-x64-18_dec_2014.tar https://docs.google.com/uc?id=1fkU8A1r7sITn2fQfR5-AWpoSvyWJ_B1C
RUN cd /usr/local; tar xf openjdk-7u75-b13-linux-x64-18_dec_2014.tar
RUN ln -s /usr/local/java-se-7u75-ri /usr/local/java
RUN mkdir /usr/java /JOE
RUN cd /JOE; git clone https://github.com/joekoolade/JOE.git .
RUN gdown  -O /usr/java/apache-ant-1.9.16-bin.tar.gz  https://docs.google.com/uc?id=1an5eWcKxlXgHJg_m9hSXByWEdzyCweIP
RUN cd /usr/java; tar xf apache-ant-1.9.16-bin.tar.gz
RUN gdown  -O /usr/java/jdk-6u45-linux-x64.bin https://docs.google.com/uc?id=1anPQ46KaBWohkMzvWsTbZxcF1cT5mvJ3
RUN chmod +x /usr/java/jdk-6u45-linux-x64.bin
RUN cd /usr/java; ./jdk-6u45-linux-x64.bin
# ENV JAVA_HOME="/usr/java/jdk1.6.0_45/"
ENV JAVA_HOME=/usr/local/java/
ENV PATH=/usr/java/apache-ant-1.9.16/bin:$PATH
