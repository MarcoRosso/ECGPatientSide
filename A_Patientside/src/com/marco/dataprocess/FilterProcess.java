package com.marco.dataprocess;



public class FilterProcess {
	private static final int ORDER= 4;
	private static int NP=648001;
	private static final double pi=3.14159265359; 
	public static void filtering(int num, double X[], double Y[], int sfreq){
		double a[] = { 1, 0, 0, 0, 0 }, 
			   b[] = { 0.2, 0.2, 0.2, 0.2, 0.2 };
		int i, j;	
		double meannum = mean(X,num);
		for (i = 0; i < num; i++)
		{
			X[i] = X[i] - meannum;
		}
		NP=num-1;
		filter(ORDER, a, b, NP, X, Y);
		double T = 1.0 / sfreq;//如果sfrep是整数，需要用1.0/，
		double Fc = 1;//截止频率
		double c1 = 1 / (1 + Math.tan(Fc*pi*T));//标准库有tan函数double tan(double x); #include<math.h>
		double c2 = (1 - Math.tan(Fc*pi*T)) / (1 + Math.tan(Fc*pi*T));
		b[0] = c1;
		b[1] = -c1;
		b[2] = 0;
		b[3] = 0;
		b[4] = 0;
		a[0] = 1;
		a[1] = -c2;
		a[2] = 0;
		a[3] = 0;
		a[4] = 0;
		filter(ORDER, a, b, NP, Y, X);
		if (sfreq == 250)
		{
			b[0] = 0.0913149004358320;
			b[1] = 0.182629800871664;
			b[2] = 0.0913149004358320;
			b[3] = 0;
			b[4] = 0;
			a[0] = 1;
			a[1] = -0.982405793108396;
			a[2] = 0.347665394851723;
			a[3] = 0;
			a[4] = 0;
		}
		else
		{
			b[0] = 0.0494899562686771;
			b[1] = 0.0989799125373541;
			b[2] = 0.0494899562686771;
			b[3] = 0;
			b[4] = 0;
			a[0] = 1;
			a[1] = -1.27963242499781;
			a[2] = 0.477592250072517;
			a[3] = 0;
			a[4] = 0;
		}
		filter(ORDER, a, b, NP, X, Y);

	}
	public static void filter(int ord, double a[], double b[], int np, double x[], double y[])//先是b再是a
	{//ord是a,b大小，np是xy数值加一
		int i, j;
		y[0] = b[0] * x[0];
		for (i = 1; i<ord + 1; i++)
		{
			y[i] = 0.0;
			for (j = 0; j<i + 1; j++)
				y[i] = y[i] + b[j] * x[i - j];
			for (j = 0; j<i; j++)
				y[i] = y[i] - a[j + 1] * y[i - j - 1];
		}
		/* end of initial part */
		for (i = ord + 1; i<np + 1; i++)
		{
			y[i] = 0.0d;
			for (j = 0; j<ord + 1; j++)
				y[i] = y[i] + b[j] * x[i - j];
			for (j = 0; j<ord; j++)
				y[i] = y[i] - a[j + 1] * y[i - j - 1];
		}
	}
	public static double mean(double x[], int num)
	{
		double sum = 0;
		for (int i = 0; i<num; i++)
		{
			sum=sum+x[i];
		}
		return(sum / num);
	}
	public static double min(double array[], int num)
	{
		int i;
		double min = array[0];
		for (i = 1; i < num; i++)
		{
			if ((array[i] - min) <= -0.000001)
				min = array[i];
		}
		return(min);
	}
	public static double max(double array[], int num)
	{
		int i;
		double max = array[0];
		for (i = 1; i < num; i++)
		{
			if ((array[i] - max) >= 0.000001)
				max = array[i];
		}
		return(max);
	}
}


