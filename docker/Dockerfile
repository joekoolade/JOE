FROM centos:7

RUN yum update -y
RUN yum install -y bash-completion bzip2 gcc gcc-c++ git make ncurses-devel patch
RUN yum install -y rsync tar unzip wget which diffutils python
RUN yum install -y flex bison
ADD openjdk-7u75-b13-linux-x64-18_dec_2014.tar.gz /usr/local
RUN ln -s /usr/local/java-se-7u75-ri /usr/local/java
RUN mkdir /usr/java /JOE
ADD apache-ant-1.9.16-bin.tar.gz /usr/java
COPY jdk-6u45-linux-x64.bin /usr/java
RUN chmod +x /usr/java/jdk-6u45-linux-x64.bin
RUN cd /usr/java; ./jdk-6u45-linux-x64.bin
# ENV JAVA_HOME="/usr/java/jdk1.6.0_45/"
ENV JAVA_HOME=/usr/local/java/
ENV PATH=/usr/java/apache-ant-1.9.16/bin:$PATH
