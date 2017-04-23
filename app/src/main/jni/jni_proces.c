//
// Created by 游思睿 on 17/3/22.
//



#include <math.h>
#include <stdio.h>
#include "cv.h"
#include "highgui.h"
#include<com_app_ysr_face_util_NdkUtil.h>
#include<android/log.h>
#define TRAIN_NUM 5
#define IMG_HEIGHT 200
#define IMG_WIDTH 180



void matrix_reverse(double *src,double *dest,int row,int col)	//转置
{
    int i,j;

    for (i=0;i<row;i++)
    {
        for (j=0;j<col;j++)
        {
            dest[i*col+j] = src[j*row+i];
        }
    }
}

void matrix_mutil(double *c,double *a,double *b,int x,int y,int z)	//矩阵乘法
{
    int i,j,k;
    for (i=0;i<x;i++)
    {
        for (k=0;k<z;k++)
        {
            for (j=0;j<y;j++)
            {
                c[i*z+k] +=a[i*y+j]*b[j*z+k];
            }
        }
    }
}


void load_data(double *T,IplImage *src,int k)
{
    int i,j;

    //一副图像压缩成一维的，存在T的一列里
    for (i=0;i<IMG_HEIGHT;i++)
    {
        for (j=0;j<IMG_WIDTH;j++)
        {
            T[(i*IMG_WIDTH+j)*TRAIN_NUM+k-1]= (double)(unsigned char)src->imageData[i*IMG_WIDTH+j];
        }
    }
}

void calc_mean(double *T,double *m)
{
    int i,j;
    double temp;

    for (i=0;i<IMG_WIDTH*IMG_HEIGHT;i++)
    {
        temp=0;
        for (j=0;j<TRAIN_NUM;j++)
        {
            temp = temp + T[i*TRAIN_NUM+j];
        }
        m[i] = temp/TRAIN_NUM;
    }
}

void calc_covariance_matrix(double *T,double *L,double *m)
{
    int i,j,k;
    double *T1;

    //T = T -m
    for (i=0;i<IMG_WIDTH*IMG_HEIGHT;i++)
    {
        for (j=0;j<TRAIN_NUM;j++)
        {
            T[i*TRAIN_NUM+j] = T[i*TRAIN_NUM+j] - m[i];
        }
    }

    T1 = (double *)malloc(sizeof(double)*IMG_HEIGHT*IMG_WIDTH*TRAIN_NUM);

    //L = T' * T
    matrix_reverse(T,T1,IMG_WIDTH*IMG_HEIGHT,TRAIN_NUM);
    matrix_mutil(L,T1,T,TRAIN_NUM,IMG_HEIGHT*IMG_WIDTH,TRAIN_NUM);

    free(T1);
}


void pick_eignevalue(double *b,double *q,double *p_q,int num_q)
{
    int i,j,k;

    k=0;//p_q的列
    for (i=0;i<TRAIN_NUM;i++)//col
    {
        if (b[i]>1)
        {
            for (j=0;j<TRAIN_NUM;j++)//row
            {
                p_q[j*num_q+k] = q[j*TRAIN_NUM+i];//按列访问q,按列存储到p_q

            }
            k++;
        }
    }
}

void get_eigenface(double *p_q,double *T,int num_q,double *projected_train,double *eigenvector)
{
    //double	*temp;
    double tmp;
    int i,j,k;
    IplImage *projected;
    char res[20]={0};	//file name

    projected = cvCreateImage(cvSize(IMG_WIDTH,IMG_HEIGHT),IPL_DEPTH_8U,1);
    //temp = (double *)malloc(sizeof(double)*IMG_HEIGHT*IMG_WIDTH*num_q);//按列存取

    memset(eigenvector,0,sizeof(double)*IMG_HEIGHT*IMG_WIDTH*num_q);
    memset(projected_train,0,sizeof(double)*TRAIN_NUM*num_q);

    //求特征脸
    //matrix_mutil(temp,T,p_q,IMG_WIDTH*IMG_HEIGHT,TRAIN_NUM,num_q);

    /*for (i=0;i<num_q;i++)
    {
        sprintf(res,"%d.jpg",i);
        for (j=0;j<IMG_HEIGHT;j++)
        {
            for (k=0;k<IMG_WIDTH;k++)
            {
                projected->imageData[j*IMG_WIDTH+k] = (unsigned char)abs(temp[(j*IMG_WIDTH+k)*num_q+i]);
            }
        }
        cvSaveImage(res,projected);
    }*/



    //求Q的特征向量X*e，矩阵相乘
    matrix_mutil(eigenvector,T,p_q,IMG_HEIGHT*IMG_WIDTH,TRAIN_NUM,num_q);

    //投影，计算特征空间变换,Eigenfaces'*A(:,i);
    matrix_reverse(eigenvector,eigenvector,IMG_WIDTH*IMG_HEIGHT,num_q);
    matrix_mutil(projected_train,eigenvector,T,num_q,IMG_WIDTH*IMG_HEIGHT,TRAIN_NUM);
}


void cstrq(double a[],int n,double q[],double b[],double c[])
{
    int i,j,k,u,v;
    double h,f,g,h2;
    for (i=0; i<=n-1; i++)
        for (j=0; j<=n-1; j++)
        { u=i*n+j; q[u]=a[u];}
    for (i=n-1; i>=1; i--)
    { h=0.0;
        if (i>1)
            for (k=0; k<=i-1; k++)
            { u=i*n+k; h=h+q[u]*q[u];}
        if (h+1.0==1.0)
        { c[i]=0.0;
            if (i==1) c[i]=q[i*n+i-1];
            b[i]=0.0;
        }
        else
        { c[i]=sqrt(h);
            u=i*n+i-1;
            if (q[u]>0.0) c[i]=-c[i];
            h=h-q[u]*c[i];
            q[u]=q[u]-c[i];
            f=0.0;
            for (j=0; j<=i-1; j++)
            { q[j*n+i]=q[i*n+j]/h;
                g=0.0;
                for (k=0; k<=j; k++)
                    g=g+q[j*n+k]*q[i*n+k];
                if (j+1<=i-1)
                    for (k=j+1; k<=i-1; k++)
                        g=g+q[k*n+j]*q[i*n+k];
                c[j]=g/h;
                f=f+g*q[j*n+i];
            }
            h2=f/(h+h);
            for (j=0; j<=i-1; j++)
            { f=q[i*n+j];
                g=c[j]-h2*f;
                c[j]=g;
                for (k=0; k<=j; k++)
                { u=j*n+k;
                    q[u]=q[u]-f*c[k]-g*q[i*n+k];
                }
            }
            b[i]=h;
        }
    }
    for (i=0; i<=n-2; i++) c[i]=c[i+1];
    c[n-1]=0.0;
    b[0]=0.0;
    for (i=0; i<=n-1; i++)
    { if ((b[i]!=0.0)&&(i-1>=0))
            for (j=0; j<=i-1; j++)
            { g=0.0;
                for (k=0; k<=i-1; k++)
                    g=g+q[i*n+k]*q[k*n+j];
                for (k=0; k<=i-1; k++)
                { u=k*n+j;
                    q[u]=q[u]-g*q[k*n+i];
                }
            }
        u=i*n+i;
        b[i]=q[u]; q[u]=1.0;
        if (i-1>=0)
            for (j=0; j<=i-1; j++)
            { q[i*n+j]=0.0; q[j*n+i]=0.0;}
    }
    return;
}

//q:特征向量，b：特征值
int csstq(int n,double b[],double c[],double q[],double eps,int l)
{
    int i,j,k,m,it,u,v;
    double d,f,h,g,p,r,e,s;
    c[n-1]=0.0; d=0.0; f=0.0;
    for (j=0; j<=n-1; j++)
    { it=0;
        h=eps*(fabs(b[j])+fabs(c[j]));
        if (h>d) d=h;
        m=j;
        while ((m<=n-1)&&(fabs(c[m])>d)) m=m+1;
        if (m!=j)
        { do
            { if (it==l)
                { printf("fail\n");
                    return(-1);
                }
                it=it+1;
                g=b[j];
                p=(b[j+1]-g)/(2.0*c[j]);
                r=sqrt(p*p+1.0);
                if (p>=0.0) b[j]=c[j]/(p+r);
                else b[j]=c[j]/(p-r);
                h=g-b[j];
                for (i=j+1; i<=n-1; i++)
                    b[i]=b[i]-h;
                f=f+h; p=b[m]; e=1.0; s=0.0;
                for (i=m-1; i>=j; i--)
                { g=e*c[i]; h=e*p;
                    if (fabs(p)>=fabs(c[i]))
                    { e=c[i]/p; r=sqrt(e*e+1.0);
                        c[i+1]=s*p*r; s=e/r; e=1.0/r;
                    }
                    else
                    { e=p/c[i]; r=sqrt(e*e+1.0);
                        c[i+1]=s*c[i]*r;
                        s=1.0/r; e=e/r;
                    }
                    p=e*b[i]-s*g;
                    b[i+1]=h+s*(e*g+s*b[i]);
                    for (k=0; k<=n-1; k++)
                    { u=k*n+i+1; v=u-1;
                        h=q[u]; q[u]=s*q[v]+e*h;
                        q[v]=e*q[v]-s*h;
                    }
                }
                c[j]=s*p; b[j]=e*p;
            }
            while (fabs(c[j])>d);
        }
        b[j]=b[j]+f;
    }
    for (i=0; i<=n-1; i++)
    { k=i; p=b[i];
        if (i+1<=n-1)
        { j=i+1;
            while ((j<=n-1)&&(b[j]<=p))
            { k=j; p=b[j]; j=j+1;}
        }
        if (k!=i)
        { b[k]=b[i]; b[i]=p;
            for (j=0; j<=n-1; j++)
            { u=j*n+i; v=j*n+k;
                p=q[u]; q[u]=q[v]; q[v]=p;
            }
        }
    }
    return(1);
}


JNIEXPORT jint JNICALL Java_com_app_ysr_face_util_NdkUtil_getCmpPic
(JNIEnv *env, jclass obj){
    double *T,*L,*m,*b,*q,*c,*p_q,*projected_train,*T_test,*projected_test,*eigenvector,*Euc_dist;
    double eps,temp;
    int i,j,flag,iteration,num_q;
    char res[50];
    IplImage *tmp_img,*test_img;

    T = (double *)malloc(sizeof(double)*IMG_HEIGHT*IMG_WIDTH*TRAIN_NUM);	//原始数据
    T_test = (double *)malloc(sizeof(double)*IMG_HEIGHT*IMG_WIDTH*1);		//测试数据
    m = (double *)malloc(sizeof(double)*IMG_HEIGHT*IMG_WIDTH);		//平均值
    L = (double *)malloc(sizeof(double)*TRAIN_NUM*TRAIN_NUM);		//L=T'*T，协方差矩阵
    b = (double *)malloc(sizeof(double)*TRAIN_NUM);				//L的特征值
    q = (double *)malloc(sizeof(double)*TRAIN_NUM*TRAIN_NUM);	//L特征值对应的特征向量
    c = (double *)malloc(sizeof(double)*TRAIN_NUM);				//实对称三对角矩阵的次对角线元素

    eps = 0.000001;
    memset(L,0,sizeof(double)*TRAIN_NUM*TRAIN_NUM);

    //存储图像数据到T矩阵

    for (i=1;i<=TRAIN_NUM;i++)
    {
        sprintf(res,"/sdcard/FaceDetect/%d.jpg",i);
        tmp_img = cvLoadImage(res,CV_LOAD_IMAGE_GRAYSCALE);
        load_data(T,tmp_img,i);
    }

    //求T矩阵行的平均值
    calc_mean(T,m);

    //构造协方差矩阵
    calc_covariance_matrix(T,L,m);

    //求L的特征值，特征向量
    iteration = 60;
    cstrq(L,TRAIN_NUM,q,b,c);
    flag = csstq(TRAIN_NUM,b,c,q,eps,iteration); //数组q中第j列为数组b中第j个特征值对应的特征向量
    if (flag<0)
    {
        __android_log_print(ANDROID_LOG_ERROR,"JniLog","fucking failed!\n");
    }else
    {
        __android_log_print(ANDROID_LOG_ERROR,"JniLog","success to get eigen value and vector\n");
    }

    //对L挑选合适的特征值，过滤特征向量
    num_q=0;
    for (i=0;i<TRAIN_NUM;i++)
    {
        if (b[i]>1)
        {
            num_q++;

        }
    }
    p_q = (double *)malloc(sizeof(double)*TRAIN_NUM*TRAIN_NUM);			//挑选后的L的特征向量，仅过滤，未排序
    projected_train = (double *)malloc(sizeof(double)*TRAIN_NUM*num_q);	//投影后的训练样本特征空间
    eigenvector = (double *)malloc(sizeof(double)*IMG_HEIGHT*IMG_WIDTH*num_q);//Pe=λe,Q(Xe)=λ(Xe)，投影变换向量
    pick_eignevalue(b,q,p_q,num_q);
    get_eigenface(p_q,T,num_q,projected_train,eigenvector);

    //读取测试图像
    //sprintf(res,"/sdcard/%d.jpg",n);
    test_img = cvLoadImage("/sdcard/FaceDetect/100.jpg",CV_LOAD_IMAGE_GRAYSCALE);
    projected_test = (double *)malloc(sizeof(double)*num_q*1);//在特征空间投影后的测试样本


    for (i=0;i<IMG_HEIGHT;i++)
    {
        for (j=0;j<IMG_WIDTH;j++)
        {
            T_test[i*IMG_WIDTH+j] = (double)(unsigned char)test_img->imageData[i*IMG_WIDTH+j] - m[i*IMG_WIDTH+j];
        }
    }

    //将待测数据投影到特征空间
    memset(projected_test,0,sizeof(double)*num_q);
    matrix_mutil(projected_test,eigenvector,T_test,num_q,IMG_WIDTH*IMG_HEIGHT,1);

    //计算projected_test与projected_train中每个向量的欧氏距离
    Euc_dist = (double *)malloc(sizeof(double)*TRAIN_NUM);
    for (i=0;i<TRAIN_NUM;i++)
    {
        temp = 0;
        for (j=0;j<num_q;j++)
        {
            temp = temp + (projected_test[j]-projected_train[j*TRAIN_NUM+i])*(projected_test[j]-projected_train[j*TRAIN_NUM+i]);
        }
        Euc_dist[i] = temp;
        //printf("%f \n",temp);
    }
    //寻找最小距离
    double min = Euc_dist[0];
    int label;
    __android_log_print(ANDROID_LOG_ERROR,"JniLog","欧式距离:\n");
    for (i=0;i<TRAIN_NUM;i++)
    {
        __android_log_print(ANDROID_LOG_ERROR,"JniLog","%lf\n",Euc_dist[i]);
        if (min>=Euc_dist[i])
        {
            min = Euc_dist[i];
            label = i;
        }
    }
    __android_log_print(ANDROID_LOG_ERROR,"JniLog","图片编号%d",label+1);
    return label+1;
}



