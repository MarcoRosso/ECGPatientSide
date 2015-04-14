package com.marco.dataprocess;


public class QRSProcess {
	public static void differFilter(double x[], double y[], int num)//一阶差分运算函数
	{
		int i;
		double[] y1 = new double[num]; 
		double[] y2 = new double[num];

		for (i = 0; i < num;i++)
		{
			y1[i] = x[i] * x[i];
		}
		//y1 = x. ^ 2;
		double numd[] = {1,-1};//[1 - 1];
		double dend[] = {1,0};// [1 0];
		//先是b再是a      filter(1, numd, dend, num+1, y1, y2);//	y = filter(numd, dend, y1);// y=y1[n+2]-y1[n]
		FilterProcess.filter(1, dend, numd, num-1, y1, y2);
		for (i = 0; i < num; i++)
		{
			y[i] = y2[i];
		}
	}
	public static double sum(double array[], int num)
	{
		int i;
		double sum = 0;
		for (i = 0; i < num; i++)
		{
			sum += array[i];
		}
		return(sum);
	}
	public static double sumint(int array[], int num)
	{
		int i;
		double sum = 0;
		for (i = 0; i < num; i++)
		{
			sum += array[i];
		}
		return(sum);
	}
	public static int RPeekDetect(double x[], int num, int fs, int RIndex[])//*RIndex = (int*)malloc(num*sizeof(int))//int *a; a = *RIndex;if(a !=*RIndex){free(a)}
	{
		int i,j;

		double[] xDiff = new double[num+1];//内存泄露
		differFilter(x, xDiff, num);

		int RIndexNo = -1;
		//*RIndex = (int*)malloc(num*sizeof(int));//好像是这样
		//memset(*RIndex,0,num*sizeof(int));
		double minThresholdAry[] = {0,0,0,0,0,0};// zeros(1, 6);
		int rrIntervalAry[] = {0,0,0,0,0};// zeros(1, 5);
		double maxThresholdAry[] = {0,0,0,0,0,0};//zeros(1, 6);


		double minTemp[] = {0,0,0,0,0};
		double maxTemp[] = {0,0,0,0,0};
		double[] array = new double[2*fs];
		double[] array_2 = new double[2*fs];
		double[] array_3 = new double[2*fs];

		/*double array[] = (double*)malloc(2 * fs*sizeof(double));
		if (array == NULL)
		{
			printf("array space error!");
			return -1;
		}
		double *array_2 = (double*)malloc(2 * fs*sizeof(double));
		if (array == NULL)
		{
			printf("array_2 space error!");
			return -1;
		}
		double *array_3 = (double*)malloc(2 * fs*sizeof(double));
		if (array == NULL)
		{
			printf("array_3 space error!");
			return -1;
		}*/
		for (i = 0; i < 5;i++)
		{
			int beginIndex = i * 2 * fs;//(i - 1) * 2 / Ts + 1; % For 10 Seconds divided into 5 groups
			//int endIndex = i * 2 * fs;

			for (j = 0; j < 2 * fs; j++)// j < 2 * fs;
			{
				array[j] = xDiff[beginIndex + j];	
			}
			//array = xDiff(beginIndex:endIndex);
			for (j = 0; j < 2 * fs; j++)
			{
				array_2[j] = x[beginIndex + j];
			}
			//array_2 = x(beginIndex:endIndex);
			minTemp[i] = FilterProcess.min(array, 2*fs);
			maxTemp[i] = FilterProcess.max(array_2, 2*fs);
		}

		double minThreshold = 0.5 * (sum(minTemp, 5) - FilterProcess.max(minTemp, 5) - FilterProcess.min(minTemp, 5)) / 3;//(length(minTemp) - 2);
		double maxThreshold = 0.42 * sum(maxTemp, 5) / 5;//length(maxTemp);

		j = 1;
		int jTemp,RPeekIndex;

		while ((RIndexNo < 5) && (j < num - 1))//这里把5改成4了
		{
			if (xDiff[j] < minThreshold)
			{
				jTemp = j;
				while ((jTemp>-1) && ((Math.abs(xDiff[jTemp]) - Math.abs(xDiff[jTemp - 1]) > 0.000001)))
					jTemp--;
				if (jTemp > -1)
				{
					RPeekIndex = jTemp;//这里没有加一，因为下标不同，Index也不同
					jTemp = j;
					while ((jTemp < num) && (Math.abs(xDiff[jTemp]) - Math.abs(xDiff[jTemp + 1]) < -0.000001))
						jTemp++;
					if (jTemp < num -1)
					{
						RIndexNo++;
						RIndex[RIndexNo] = RPeekIndex;
						minThresholdAry[RIndexNo] = xDiff[jTemp];
						if (RIndexNo > 0)
							rrIntervalAry[RIndexNo - 1] = RIndex[RIndexNo] - RIndex[RIndexNo - 1];
					}
					j = jTemp + (int)(0.2 * fs + 0.5);
				}
				else
				{
					while ((jTemp < num -1) && ((Math.abs(xDiff[jTemp]) - Math.abs(xDiff[jTemp + 1])) < -0.000001))
						jTemp++;
					j = jTemp + (int)(0.2 * fs + 0.5);//四舍五入强制取值
				}
			}
			j++;
		}

		minThreshold = 0.5 * ((sum(minThresholdAry, 6) - FilterProcess.max(minThresholdAry, 6) - FilterProcess.min(minThresholdAry, 6)) / 4);//(length(minThresholdAry) - 2));
		double rrInterval = sumint(rrIntervalAry, 5) / 5.0;//length(rrIntervalAry);

		for (int a = 0; a < num; a++)
		{
			xDiff[num - a] = xDiff[num -a -1];
		}

		for (int a = 0; a < num; a++)
		{
			x[num - a] = x[num - a - 1];
		}

		for (int a = 0; a < num; a++)
		{
			RIndex[num - a] = RIndex[num - a -1] + 1;
		}

		int m = RIndex[RIndexNo] + (int)(0.2 * fs + 0.5) + 1;
		//RIndexNo++;//加上这句就错了，不要加
		int mTemp;
		int repeatTime = 0;
		int doSkip = 0;

		//maxtemp值在此计算然后后面只用加法就可以了；
		double summaxtemp = 0;
		for (i = 0; i < 5;i++)
		{
			summaxtemp += maxTemp[i];
		}

		while (m < num)
		{
			if (((xDiff[m] - minThreshold) < -0.000001) && ((x[m] - maxThreshold) > 0.000001))
			{
				mTemp = m;
				while ((Math.abs(xDiff[mTemp]) - Math.abs(xDiff[mTemp - 1])) > 0.000001)
					mTemp--;
				RPeekIndex = mTemp;
				if ((((RPeekIndex - RIndex[RIndexNo]) - 1.66*rrInterval) > 0.000001) && (repeatTime < 6))
				{
					//printf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa%lf", rrInterval);
					repeatTime++;
					for (i = 0; i < repeatTime; i++)
					{
						minThreshold = minThreshold * 0.8;
					}
					m = RIndex[RIndexNo] + (int)(0.2 * fs + 0.5);// % int32(0.5*rrInterval)
					doSkip = 1;
				}


	/*			else if((((RPeekIndex - (*RIndex)[RIndexNo]) - 0.6*rrInterval) < -0.000001) && (doSkip == 0))
				{
					repeatTime = 0;
					mTemp = m;
					while ((mTemp < num) && ((xDiff[mTemp] - minThreshold) < -0.000001))
						mTemp++;
					m = mTemp + (int)(0.2 * fs + 0.5); //% int32(0.5*rrInterval)
				}
	*///永远运行不到

				else
				{
					doSkip = 0;
					repeatTime = 0;
					RIndexNo++;
					RIndex[RIndexNo] = RPeekIndex;
					int k = (int)(RPeekIndex / 360.0 + 0.5);
					int ktemp = k;
					if (k != ktemp)
					{
						for (i = k - ktemp; i > 0; i--)
						{
							int beginNum = (k - i) * fs;
							for (j = 0; j < fs; j++)
							{
								array_3[j] = x[beginNum + j];
							}
							maxTemp[2] = maxTemp[3];
							maxTemp[3] = maxTemp[4];
							maxTemp[4] = FilterProcess.max(array_3, fs);
							summaxtemp += maxTemp[4];
						}
					}

					maxThreshold =0.42 * summaxtemp / 5;//0.42 * sum(maxTemp, 5) / 5;//改length(maxTemp);
					for (i = 0; i < 5; i++)
					{
						minThresholdAry[i] = minThresholdAry[i+1];
					}
					mTemp = m;
					while ((mTemp < num) && ((Math.abs(xDiff[mTemp]) - Math.abs(xDiff[mTemp + 1])) < -0.000001))
						mTemp++;
//					if (mTemp < num)
					minThresholdAry[5] = xDiff[mTemp];
//					else
//					{
//						minThresholdAry[5] = 0;//永远运行不到
//					}
					for (i = 0; i < 4; i++)
					{
						rrIntervalAry[i] = rrIntervalAry[i + 1];
					}
					//rrIntervalAry(1:(length(rrIntervalAry) - 1))= rrIntervalAry(2:length(rrIntervalAry));
					rrIntervalAry[4] = RIndex[RIndexNo] - RIndex[RIndexNo - 1];
					minThreshold = 0.5 * ((sum(minThresholdAry, 6) - FilterProcess.max(minThresholdAry, 6) - FilterProcess.min(minThresholdAry, 6)) / 4);
					rrInterval = sumint(rrIntervalAry, 5) / 5.0;
					m = RIndex[RIndexNo] + (int)(0.2*fs + 0.5);
				}
			}
			m++;
		}
		return(RIndexNo);
	}
	public static int QPeekDetect(double x[], int num, int fs, int QIndex[],int RIndexNo, int RIndex[])
	{
		double[] xDiff = new double[num];
		differFilter(x, xDiff, num);
		int i,j,leftLimit;
		//xDiff = differFilter(x);

		//RIndex = RPeekDetect(x, Ts);
		//*QIndex = (int*)malloc(num*sizeof(int));//好像是这样
		//memset(*QIndex, 0, num*sizeof(int));
		//QIndex = zeros(1, length(xDiff));
		int QIndexNo = 0;
		//RIndex在外面做过相应调整
		for (i = 0; i < RIndexNo;i++)//(i = 1 : length(RIndex))
		{
			j = RIndex[i];
			if (i == 0)
				leftLimit = 0;
			else
				leftLimit = RIndex[i - 1];

			while ((j > leftLimit) && ((xDiff[j -1] - xDiff[j - 2]) < -0.000001))
				j = j - 1;

			if (j > leftLimit)
			{
				while ((j > leftLimit) && ((Math.abs(xDiff[j - 1]) - Math.abs(xDiff[j - 2])) > 0.000001) )
					j = j - 1;
				if (j > leftLimit)
				{
					QIndexNo++;
					QIndex[QIndexNo - 1] = j -1;//不知道什么地方j多了一个一，暂时先在这里减去了
				}
			}
		}
		//QIndex = QIndex(1:QIndexNo);
		return(QIndexNo);
	}
	public static int SPeekDetect(double x[], int num, int fs, int SIndex[], int RIndexNo, int RIndex[])
	{
		//xDiff = differFilter(x);

		//RIndex = RPeekDetect(x, Ts);
		//SIndex = zeros(1, length(xDiff));
		//SIndexNo = 0;

		double[] xDiff = new double[num];
		differFilter(x, xDiff, num);
		int i, j, rightLimit;
		//xDiff = differFilter(x);

		//RIndex = RPeekDetect(x, Ts);
		//*SIndex = (int*)malloc(num*sizeof(int));//好像是这样
		//memset(*SIndex, 0, num*sizeof(int));
		//QIndex = zeros(1, length(xDiff));
		int SIndexNo = 0;
		//RIndex在外面做过相应调整
		for (i = 0; i < RIndexNo; i++)//(i = 1 : length(RIndex))
		{
			//for i = 1:length(RIndex),
			j = RIndex[i];
			if (i == (RIndexNo - 1))
				rightLimit = num;
			else
				rightLimit = RIndex[i + 1];

			while ((j < rightLimit) && ((xDiff[j - 1] - xDiff[j]) > 0.000001))
				j++;

			if (j < rightLimit)
			{
				while ((j < rightLimit) && ((Math.abs(xDiff[j - 1]) - Math.abs(xDiff[j])) > 0.000001))
					j = j + 1;

				if (j < rightLimit)
				{
					SIndexNo++;
					SIndex[SIndexNo - 1] = j - 1;//又不知道是怎么多了一，所以现在这里减去
				}
			}
		}

		return(SIndexNo);
		//SIndex = SIndex(1:SIndexNo);
	}


}
