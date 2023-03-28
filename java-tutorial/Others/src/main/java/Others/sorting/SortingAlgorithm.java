package Others.sorting;

import java.util.Arrays;

public class SortingAlgorithm
{
    private int partition(int array[], int start, int end)
    {
        int pivot = array[end];
        int partitionIndex = start;
        for (int i = start; i < end; i++)
        {
            if (array[i] <= pivot)
            {
                int temp = array[i];
                array[i] = array[partitionIndex];
                array[partitionIndex] = temp;
                partitionIndex++;
            }
        }
        int temp = array[partitionIndex];
        array[partitionIndex] = array[end];
        array[end] = temp;
        return partitionIndex;
    }

    public void quickSort(int array[], int start, int end)
    {
        if (start < end)
        {
            int partition = partition(array, start, end);
            quickSort(array, start, partition - 1);
            quickSort(array, partition + 1, end);
        }

    }

    public void mergeSort(int array[])
    {
        int n = array.length;
        if (n < 2)
        {
            return;
        }
        int mid = n / 2;
        int leftArray[] = new int[mid]; // O(n)
        int rightArray[] = new int[n - mid]; // O(n)
        for (int i = 0; i < mid; i++)
        {
            leftArray[i] = array[i];
        }
        for (int j = mid; j < n; j++)
        {
            rightArray[j - mid] = array[j];
        }
        mergeSort(leftArray);
        mergeSort(rightArray);
        mergeArrays(array, leftArray, rightArray);
    }

    private void mergeArrays(int array[], int leftArray[], int rightArray[])
    {
        int i = 0, j = 0, k = 0;
        int n = array.length, nL = leftArray.length, nR = rightArray.length;
        while (i < nL && j < nR)
        {
            if (leftArray[i] <= rightArray[j])
            {
                array[k++] = leftArray[i++];
            }
            else
            {
                array[k++] = rightArray[j++];
            }
        }
        while (i < nL)
        {
            array[k++] = leftArray[i++];
        }
        while (j < nR)
        {
            array[k++] = rightArray[j++];
        }
    }

    public static void main(String[] args)
    {
        SortingAlgorithm algorithm = new SortingAlgorithm();
        int array[] = new int[]
        { 2, 5, 9, 1, 8, 0, 3, 6, 4, 7 };
        algorithm.mergeSort(array);
        algorithm.quickSort(array, 0, array.length - 1);
        System.out.println("array: " + Arrays.toString(array));
    }

}
