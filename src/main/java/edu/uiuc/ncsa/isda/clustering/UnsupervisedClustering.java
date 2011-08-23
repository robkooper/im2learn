/**
 * File: UnsupervisedClustering.java
 * Programmers: Scott Deuser, Kelby Lanning
 * Date Started: June 11, 2008
 * Last Updated: November 3, 2008
 * Description - This class implements common functionality that is useful for clustering algorithms
 * Intended Use - Kmeans.java and Isodata.java
 */
package edu.uiuc.ncsa.isda.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;


public class UnsupervisedClustering {

    //protected int Nc = 0;                               //current number of clusters.
    protected int                          numClusters;               //total number of clusters desired.
    protected int                          maxIterations;             //the maximum number of iterations for the algorithm to run.
    protected int                          numberOfDimensions;        //the number of dimensions for a point.
    protected ArrayList<Point>             setOfPoints        = null; // The set of points to be clustered.
    protected ArrayList<Cluster>           clusterList        = null; //list of current clusters
    protected ArrayList<Cluster>           clusterListOld     = null; //list of old clusters
    protected ImageObject                  labels             = null; //1-banded ImageObject that each value is the cluster that pixel belongs to.
    protected boolean                      hasLabels          = false; //True if labels are needed and false if they are not needed.
    protected double                       centersTolerance;          //The allowable amount centers can differ but still be considered equal.
    protected int                          currentIteration   = 1;    //the current iteration of the algorithm.
    protected ArrayList<Integer>           invalidDataIndices = null; //Stores the indices of any invalid data that might come with making
    //a set of points from a image object. Used to maintain the labels image.
    protected HashMap<Integer, Double[][]> labelHashMap       = null;

    /**
     * Constructor - sets default values to parameters
     */
    public UnsupervisedClustering() {
        this.numClusters = 2;
        this.maxIterations = 4;
        this.numberOfDimensions = 0;
        this.clusterList = new ArrayList<Cluster>();
        this.centersTolerance = 0;
    } //end UnsupervisedClustering()

    /**
     * Constructor
     * 
     * @param numClusters
     *            - number of clusters desired
     * @param maxIterations
     *            - number of iterations allowed.
     */
    public UnsupervisedClustering(int numClusters, int maxIterations) {
        this.numClusters = numClusters;
        this.maxIterations = maxIterations;
        this.numberOfDimensions = 0;
        this.clusterList = new ArrayList<Cluster>();
    } //end UnsupervisedClustering()	

    /**
     * @return the numClusters
     */
    public int getNumClusters() {
        return this.numClusters;
    } //end getNumClusters()

    /**
     * @param numClusters
     *            - numClusters to set
     */
    public boolean setNumCluseters(int numClusters) {
        if (numClusters <= 0) {
            return false;
        }
        this.numClusters = numClusters;
        return true;
    } //end setNumClusters()

    /**
     * @return the maxIterations
     */
    public int getMaxIterations() {
        return this.maxIterations;
    } //end getMaxIterations()

    /**
     * @param maxIterations
     *            - the maxIterations to set
     */
    public boolean setMaxIterations(int maxIterations) {
        if (maxIterations < 0) {
            return false;
        }

        this.maxIterations = maxIterations;
        return true;
    } //end setMaxIterations()

    /**
     * @return the setOfPoints
     */
    public ArrayList<Point> getSetOfPoints() {
        return setOfPoints;
    } //end getSetOfPoints()

    /**
     * @param setOfPoints
     *            the setOfPoints to set
     */
    public void setSetOfPoints(ArrayList<Point> setOfPoints) {
        if (setOfPoints.size() == 0) {
            System.err
                    .println("There were no points in the ArrayList of points that were given as a "
                            + "parameter\nin UnsupervisedClustering.setSetOfPoints");
        } else {
            this.setOfPoints = setOfPoints;
            this.numberOfDimensions = setOfPoints.get(0)
                    .getNumberOfDimensions();
        }
    } //end setSetOfPoints()

    /**
     * @return - returns a deep copy of the list of clusters.
     */
    public ArrayList<Cluster> getClusterList() {
        return new ArrayList<Cluster>(this.clusterList);
    } //end getClusterList()

    /**
     * @return - returns a deep copy of the labels
     */
    public ImageObject getLabels() {
        if (this.labels == null) {
            return null;
        }
        ImageObject deepCopy = null;
        try {
            deepCopy = ImageObject.createImage(this.labels.getNumRows(),
                    this.labels.getNumCols(), this.labels.getNumBands(),
                    this.labels.getType());
        } catch (ImageException e) {
            System.err
                    .println("Error creating ImageObject; UnsupervisedClustering.getLabels");
        }
        for (int i = 0; i < this.labels.getSize(); i++) {
            deepCopy.set(i, this.labels.getInt(i));
        }

        return deepCopy;
    } //end getLabels()

    /**
     * Returns true if an labels ImageObject is being set up.
     * 
     * @return the hasLabels
     */
    public boolean getHasLabels() {
        return this.hasLabels;
    } //end getHasLabels()

    /**
     * @param hasLabels
     */
    public void setHasLabels(boolean hasLabels) {
        this.hasLabels = hasLabels;
    } //end setHasLabels()

    /**
     * Takes the current list of clusters and extracts the centers into a list.
     * 
     * @return - a list of points of the current cluster centers
     */
    public ArrayList<Point> getClusterCenters() {
        if (this.clusterList == null) {
            System.err.println("There are no cluster centers");
        }
        ArrayList<Point> centers = new ArrayList<Point>();
        for (int i = 0; i < this.clusterList.size(); i++) {
            centers.add(this.clusterList.get(i).getClusterCenter());
        }
        return centers;
    } //end getClusterCenters	

    /**
     * gets the tolerance that the centers can differ by and still be considered
     * equal.
     * 
     * @return - centerTolerance
     */
    public double getCentersTolerance() {
        return this.centersTolerance;
    } //end getCentersTolerance()

    /**
     * sets the tolerance of the centers.
     * 
     * @param centersTolerance
     *            - the range for which the centers can differ by and still be
     *            considered equal.
     */
    public boolean setCentersTolerance(double centersTolerance) {
        if (centersTolerance < 0) {
            return false;
        }
        this.centersTolerance = centersTolerance;
        return true;
    } //setCentersTolerance()

    //if hash map is working fine then delete this
    //	/**
    //	 * 
    //	 * @return - the label hashMap
    //	 */
    //	public HashMap<Integer, Double[][]> getLabelHashMap(){
    //		this.createHashMap();
    //		return this.labelHashMap;
    //	} //end getLabelHashMap()
    //	

    /**
     * Creates the set of points based on the image object. Every pixel is a
     * point with n dimensions. The given ImageObject can have any number of
     * bands. User a helper method depending on if labels are needed or not.
     * 
     * @param imgObject
     *            - the image that is going to be clustered.
     * @param needLabels
     *            - if true, then image object is created to store what cluster
     *            each point belongs to.
     */
    public void makePoints(ImageObject imgObject, boolean needLabels) {
        this.setOfPoints = new ArrayList<Point>();
        this.hasLabels = needLabels;
        if (this.hasLabels) {
            try {
                this.makePointsWithLabels(imgObject);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
        } else {
            this.makePointsWithoutLabels(imgObject);
        }

        this.numberOfDimensions = setOfPoints.get(0).getNumberOfDimensions();
    } //end makePoints()

    /**
     * Creates the set of points based on the image object. Initializes the
     * labels ImageObject.
     * 
     * @param imgObject
     *            - the image that is going to be clustered
     */
    private void makePointsWithLabels(ImageObject imgObject) throws Exception {
        int cols = imgObject.getNumCols();
        int rows = imgObject.getNumRows();
        int totalSize = cols * rows;
        double[] pointValues = new double[imgObject.getNumBands()];

        try {
            this.labels = ImageObject.createImage(imgObject.getNumRows(),
                    imgObject.getNumCols(), 1, ImageObject.TYPE_INT);
        } catch (ImageException e) {
            System.err.println("ImageException in IsodataAlgorithm.makePoints");
        }
        this.invalidDataIndices = new ArrayList<Integer>();
        //all data in the labels image starts off as invalid.
        this.labels.setInvalidData(-1);
        this.labels.setData(-1);

        double invalidData = imgObject.getInvalidData();
        boolean pixelValid = true;

        //if any band in a pixel is invalid then the pixel is invalid and not added to the set of points
        for (int i = 0; i < totalSize; i++) {
            for (int j = 0; j < imgObject.getNumBands(); j++) {
                if (pixelValid
                        && (imgObject.getDouble(i / cols, i % cols, j) != invalidData)
                        && (!Double.isNaN(imgObject.getDouble(i / cols, i
                                % cols, j)))) {
                    pointValues[j] = imgObject.getDouble(i / cols, i % cols, j);
                    if (Double.isNaN(pointValues[j])) {
                        throw new Exception(
                                "A point with a value of Nan is trying to be inserted into the list of points");
                    }
                } else {
                    pixelValid = false;
                }
            }
            if (pixelValid) {
                this.setOfPoints.add(new Point(pointValues));
            } else {
                this.invalidDataIndices.add(i);
                pixelValid = true;
            }
        }
    } //end makePointsWithLabels()

    /**
     * Creates the set of points based on the image object.
     * 
     * @param imgObject
     *            - the image that is going to be clustered.
     */
    private void makePointsWithoutLabels(ImageObject imgObject) {
        int cols = imgObject.getNumCols();
        int rows = imgObject.getNumRows();
        int totalSize = cols * rows;
        double[] pointValues = new double[imgObject.getNumBands()];

        double invalidData = imgObject.getInvalidData();
        boolean pixelValid = true;
        //if a pixel as any invalid data in it, then do not add it to the set of points

        for (int i = 0; i < totalSize; i++) {
            for (int j = 0; j < imgObject.getNumBands(); j++) {
                if (pixelValid
                        && (imgObject.getDouble(i / cols, i % cols, j) != invalidData)) {
                    pointValues[j] = imgObject.getDouble(i / cols, i % cols, j);
                } else {
                    pixelValid = false;
                }
            }
            if (pixelValid) {
                this.setOfPoints.add(new Point(pointValues));
            } else {
                pixelValid = true;
            }
        }
    } //end makePointsWithoutLabels()

    /**
     * Loads a set of points defined by the user as the initial cluster centers.
     * 
     * @param centers
     *            - A list of points that are going to be the initial cluster
     *            centers.
     */
    public ArrayList<Point> loadUserDefinedCenters(ArrayList<Point> centers) {
        this.clusterList.clear();
        for (int i = 0; i < centers.size(); i++) {
            this.clusterList.add(new Cluster(centers.get(i)));
        }
        return this.getClusterCenters();
    } //end loadFromFile()

    /**
     * Takes the first "numClusters" Points that are not equal to each other and
     * makes them cluster centers.
     * 
     * @return - The points that are now the cluster centers.
     * @throws - Exception is thrown if the set of points has not been set up
     *         yet.
     */
    public ArrayList<Point> firstNPoints() throws Exception {
        if (this.setOfPoints == null) {
            throw new Exception("There are no points in which to find seeds");
        }

        this.clusterList.clear();
        int indexShift = 0;
        int index = 0;
        boolean isTheSame = false;

        //add the first point
        this.clusterList.add(new Cluster(this.setOfPoints.get(index++)));

        while ((index < this.numClusters)
                && ((index + indexShift) < setOfPoints.size())) {
            //make sure each center is unique 
            for (int j = 0; j < index; j++) {
                if (this.clusterList.get(j).getClusterCenter().equals(
                        this.setOfPoints.get(index + indexShift))) {
                    isTheSame = true;
                }
            }
            if (isTheSame) {
                //try the next point
                indexShift++;
                isTheSame = false;
            } else {
                this.clusterList.add(new Cluster(this.setOfPoints.get(index
                        + indexShift)));
                index++;
                isTheSame = false;
            }
        }//end while
        return this.getClusterCenters();
    } //end firstNPoints()

    /**
     * Takes "numClusters" randomly from the set of points that are not equal to
     * each other and makes them the initial cluster centers.
     * 
     * @return - the points that are initially the centers of the clusters.
     * @throws Exception
     *             if there is not a set of points initialized.
     */
    public ArrayList<Point> random() throws Exception {
        if (this.setOfPoints == null) {
            throw new Exception("There are no points in which to find seeds");
        }

        int infiniteLoop = 1;
        this.clusterList.clear();
        boolean isTheSame = false;
        Random rand = new Random();
        int randIndex = rand.nextInt(this.setOfPoints.size());
        //add 1st center to the list
        this.clusterList.add(new Cluster(this.setOfPoints.get(randIndex)));

        //To avoid the small possibility of an infinite loop from one of the at least two situations 
        //that would cause it, infiniteLoop is introduced
        while ((this.clusterList.size() < this.numClusters)
                && (infiniteLoop++ < 100000)) {
            //make sure each center is unique 
            for (int j = 0; j < this.clusterList.size(); j++) {
                if (this.clusterList.get(j).getClusterCenter().equals(
                        this.setOfPoints.get(randIndex))) {
                    isTheSame = true;
                }
            }
            if (isTheSame) {
                //get another random index
                randIndex = rand.nextInt(this.setOfPoints.size());
                isTheSame = false;
            } else {
                //make the point a center and add it to the list
                this.clusterList.add(new Cluster(this.setOfPoints
                        .get(randIndex)));
                isTheSame = false;
            }
        } //end while

        return this.getClusterCenters();
    } //end random()

    /**
     * distributes the points among the clusters based on which cluster center
     * they are closest to
     */
    protected void distributeSamples() {
        if (this.invalidDataIndices.size() == 0) {
            distributeWithoutInvalidData();
        } else {
            distributeWithInvalidData();
        }

    }//end distributeSamples()

    /**
     * distributes the points among the clusters based on which cluster center
     * they are closest to including invalid data
     */
    private void distributeWithInvalidData() {
        int K = this.clusterList.size(); //the number of cluster centers
        //clear all Points from clusters for a fresh beginning
        for (int i = 0; i < K; i++) {
            this.clusterList.get(i).clearPoints();
        }

        double[] distances = new double[K];
        int invalidShift = 0;

        for (int i = 0; i < (this.setOfPoints.size() + this.invalidDataIndices
                .size()); i++) {
            if (i == this.invalidDataIndices.get(invalidShift)) {
                this.labels.set(i, K); // + 1);
                invalidShift++;
            } else {
                //calculate the distance from each center
                for (int centerNumber = 0; centerNumber < K; centerNumber++) {
                    distances[centerNumber] = calculateDistance(
                            this.setOfPoints.get(i - invalidShift),
                            this.clusterList.get(centerNumber)
                                    .getClusterCenter());
                }
                //find the index of the smallest distance
                int index = findIndexOfSmallestValue(distances);

                if (this.hasLabels) {
                    //set cluster number for the given point
                    this.labels.set(i, index);
                }

                //add to appropriate cluster
                clusterList.get(index).addPointToCluster(
                        this.setOfPoints.get(i - invalidShift));
            }
        }
    } //end distributeWithInvalidData()

    /**
     * distributes the points among the clusters based on which cluster center
     * they are closest to excluding invalid data
     */
    private void distributeWithoutInvalidData() {
        int K = this.clusterList.size(); //the number of cluster centers
        //clear all Points from clusters for a fresh beginning
        for (int i = 0; i < K; i++) {
            this.clusterList.get(i).clearPoints();
        }

        double[] distances = new double[K];
        for (int i = 0; i < this.setOfPoints.size(); i++) {

            //calculate the distance from each center
            for (int j = 0; j < K; j++) {
                distances[j] = calculateDistance(this.setOfPoints.get(i),
                        this.clusterList.get(j).getClusterCenter());
            }
            //find the index of the smallest distance
            int index = findIndexOfSmallestValue(distances);

            if (this.hasLabels) {
                //set cluster number for the given point
                this.labels.set(i, index);
            }

            //add to appropriate cluster
            clusterList.get(index).addPointToCluster(this.setOfPoints.get(i));
        }
    } //end distributeWithoutInvalidData()

    /**
     * Calculated the distance between the points. Points can have i dimensions
     * where i > 0
     * 
     * @param p1
     *            - a point
     * @param p2
     *            - another point
     * @return - the distance between the points
     */
    protected double calculateDistance(Point p1, Point p2) {
        double radicalExpression = 0;
        try {
            for (int i = 0; i < p1.getNumberOfDimensions(); i++) {
                radicalExpression += Math.pow((p1.getOrdinate(i) - p2
                        .getOrdinate(i)), 2);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return Math.sqrt(radicalExpression);
    }//end caculateDistance()

    /**
     * Given a list of values, it will find the index of the smallest value in
     * the list
     * 
     * @param numbers
     *            - a list of values
     * @return - the index of the smallest value in the array
     */
    protected int findIndexOfSmallestValue(double[] numbers) {
        int index = 0;
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < numbers[index]) {
                index = i;
            }
        }
        return index;
    }//end findIndexOfSmallestValue()

    /**
     * Calculates the new centers. Note, that the points have not been
     * redistributed, just the centers have changed.
     * 
     * @return - a list with just the old cluster centers in it.
     */
    protected ArrayList<Cluster> calculateClusterCenters() throws Exception {
        int Nc = this.clusterList.size(); //current number of clusters
        ArrayList<Cluster> oldClusters = new ArrayList<Cluster>();
        for (int i = 0; i < this.clusterList.size(); i++) {
            oldClusters.add(new Cluster(new Point(this.clusterList.get(i)
                    .getClusterCenter())));
        }
        int Np = 0; //number of points in a cluster
        Point tempPt;

        int j = 0;
        int numOfDimensions = 0;
        try {
            for (int k = 0; k < Nc; k++) {
                if (this.clusterList.get(k).getNumberOfPointsInCluster() > 0) {
                    j = k;
                    k = Nc;
                }
            }
            numOfDimensions = this.clusterList.get(j).getPoint(0)
                    .getNumberOfDimensions();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        double[] sum = new double[numOfDimensions];
        //cycle through each cluster
        for (j = 0; j < Nc; j++) {
            Np = this.clusterList.get(j).getNumberOfPointsInCluster();
            //cycle through each dimension
            for (int dimension = 0; dimension < numOfDimensions; dimension++) {
                sum[dimension] = 0;
                //go through point in the cluster
                for (int i = 0; i < Np; i++) {
                    try {
                        sum[dimension] += this.clusterList.get(j).getPoint(i)
                                .getOrdinate(dimension);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
                sum[dimension] /= Np;
            }
            //			if(sum[j] == Double.NaN){
            //				throw new Exception("Sum = NaN");
            //			}

            tempPt = new Point(sum);

            try {
                this.clusterList.get(j).setClusterCenter(tempPt);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
        }//end for(each cluster)

        return oldClusters;
    }//end calculateClusterCenters()

    /**
     * Checks to see if all the cluster centers in one list are equal to all the
     * cluster centers in the old list
     * 
     * @param list1
     *            - a list of clusters
     * @param list2
     *            - a list of clusters
     * @return - true if both lists have the same cluster centers false
     *         otherwise
     */
    protected boolean checkCentersEqual(ArrayList<Cluster> list1,
            ArrayList<Cluster> list2) {
        for (int i = 0; i < list1.size(); i++) {
            //check each cluster with the corresponding cluster
            if (!list1.get(i).getClusterCenter().equals(
                    list2.get(i).getClusterCenter(), this.centersTolerance)) {
                return false;
            }
        }
        return true;
    }//end checkCentersEqual()

    //if hash map is working fine then delete this
    //	protected void createHashMap(){
    //		this.labelHashMap = new HashMap<Integer, Double[][]>();
    //		
    //		Double[][] minMax = new Double[this.clusterList.get(0).getClusterCenter().getNumberOfDimensions()][2];
    //		int labelNumber;
    //					
    //			
    //		//loop through each cluster and find the minimum value of each band in a cluster
    //		//and the maximum value of each band in the cluster
    //		for(int i = 0; i < this.clusterList.size(); i++){
    //			minMax[0] = this.findMinValues(this.clusterList.get(i));
    //			minMax[1] = this.findMaxValues(this.clusterList.get(i));
    //			
    //			this.labelHashMap.put(new Integer(i), minMax);
    //			
    //		}
    //		
    //	} //end createHashMap()

    /**
     * Goes through the desired cluster and finds the smallest values from all
     * the points for each of the dimensions
     * 
     * @param c
     *            - the cluster to search through
     * @return - the array of smallest values
     */
    private Double[] findMinValues(Cluster c) {
        Double[] minValues = new Double[c.getClusterCenter()
                .getNumberOfDimensions()];

        for (int i = 0; i < minValues.length; i++) {
            minValues[i] = Double.MAX_VALUE;
        }

        //cycles through each dimension
        for (int j = 0; j < c.getClusterCenter().getNumberOfDimensions(); j++) {
            //for a given dimension go through every point
            for (int i = 0; i < c.getNumberOfPointsInCluster(); i++) {
                try {
                    if (c.getPoint(i).getOrdinate(j) < minValues[j]) {
                        minValues[j] = c.getPoint(i).getOrdinate(j);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        }

        return minValues;
    } //end minValue

    /**
     * Goes through the desired cluster and finds the largest values from all
     * the points for each of the dimensions
     * 
     * @param c
     *            - the cluster to search through
     * @return - the array of largest values
     */
    private Double[] findMaxValues(Cluster c) {
        Double[] maxValues = new Double[c.getClusterCenter()
                .getNumberOfDimensions()];

        for (int i = 0; i < maxValues.length; i++) {
            maxValues[i] = -1 * Double.MAX_VALUE;
        }

        //cycles through each dimension
        for (int j = 0; j < c.getClusterCenter().getNumberOfDimensions(); j++) {
            //for a given dimension go through every point
            for (int i = 0; i < c.getNumberOfPointsInCluster(); i++) {
                try {
                    if (c.getPoint(i).getOrdinate(j) > maxValues[j]) {
                        maxValues[j] = c.getPoint(i).getOrdinate(j);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        }

        return maxValues;
    } //end minValue

    /**
     * Computes the distance between each centroid to every other centroid
     * 
     * @param centers
     *            - list of all the cluster centers
     * 
     * @return - an ArrayList of all the distances between centroids
     * 
     * @throws exception
     *             if ArrayList index is out of bounds
     */
    public ArrayList<Double> ComputeCentroidsDist(ArrayList<Point> centers)
            throws Exception {
        if (centers != null) {
            int i, j, k = 0;
            int bands = centers.get(0).getNumberOfDimensions();
            double dist, tempDist = 0;
            int index = 0;
            ArrayList<Double> centroidDist = new ArrayList<Double>();
            for (i = 0; i < centers.size(); i++) {
                for (j = i + 1; j < centers.size(); j++) {
                    k = 0;
                    dist = 0;
                    tempDist = 0;
                    //Euclidean distance
                    while (k < bands) {
                        tempDist = centers.get(i).getOrdinate(k)
                                - centers.get(j).getOrdinate(k);
                        tempDist = tempDist * tempDist;
                        dist = dist + tempDist;
                        k++;
                    }
                    dist = Math.sqrt(dist);
                    centroidDist.add(index, dist);
                    //System.out.println(centroidDist.get(index));
                    index++;
                }
            }
            return centroidDist;
        } else {
            return null;
        }
    }

    /**
     * Computes the average distance from each point in the cluster to its
     * cluster center for each cluster
     * 
     * @param clusters
     *            - List of the clusters
     * @param centers
     *            - List of the cluster centers
     * @return - an array of the average distances for each cluster
     * @throws Exception
     *             - if the index is out of bounds
     */
    public double[] ComputeAverageDistanceFromClusterCenter(
            ArrayList<Cluster> clusters, ArrayList<Point> centers)
            throws Exception {
        if ((centers != null) && (clusters != null)) {
            int i, j, k = 0;
            int bands = centers.get(0).getNumberOfDimensions();
            double dist, totalDist, tempDist = 0;
            double[] averageDist = new double[centers.size()];
            for (i = 0; i < centers.size(); i++) {
                totalDist = 0;
                for (j = 0; j < clusters.get(i).getNumberOfPointsInCluster(); j++) {
                    k = 0;
                    dist = 0;
                    tempDist = 0;
                    //Euclidean distance
                    while (k < bands) {
                        tempDist = centers.get(i).getOrdinate(k)
                                - clusters.get(i).getPoint(j).getOrdinate(k);
                        tempDist = tempDist * tempDist;
                        dist = dist + tempDist;
                        k++;
                    }
                    dist = Math.sqrt(dist);
                    totalDist = totalDist + dist;
                    //System.out.println(centroidDist.get(index));
                }
                //figure out average for this cluster
                averageDist[i] = totalDist
                        / clusters.get(i).getNumberOfPointsInCluster();
            }
            return averageDist;
        } else {
            return null;
        }
    }

    /**
     * Finds the standard deviation vector for each cluster. - The stDevVec is
     * the standard deviation of the distances from each point to the cluster
     * center for each band in each cluster - For example [34, 25, 2] is a
     * cluster with 3 bands and 34, 25, 2 are the standard deviations of the
     * distances for each band in the cluster
     * 
     * @param clusters
     *            - list of all the clusters
     * @param centers
     *            - list of all the cluster centers
     * @return - a 2-dimensional array of all the standard deviation vectors for
     *         each cluster
     * @throws Exception
     *             - if ArrayList index is out of bounds
     */
    public double[][] FindStDevCluster(ArrayList<Cluster> clusters,
            ArrayList<Point> centers) throws Exception {
        if ((centers != null) && (clusters != null)) {
            int i, j, k = 0;
            int bands = centers.get(0).getNumberOfDimensions();
            double[][] StDevVectors = new double[centers.size()][bands];
            double temp, sum = 0;
            for (i = 0; i < centers.size(); i++) {
                sum = 0;
                for (j = 0; j < bands; j++) {
                    temp = 0;
                    for (k = 0; k < clusters.get(i)
                            .getNumberOfPointsInCluster(); k++) {
                        temp = clusters.get(i).getPoint(k).getOrdinate(j)
                                - centers.get(i).getOrdinate(j);
                        temp = temp * temp;
                        sum = sum + temp;
                    }
                    StDevVectors[i][j] = Math.sqrt(sum
                            / clusters.get(i).getNumberOfPointsInCluster());
                }
            }
            return StDevVectors;
        }

        else {
            return null;
        }
    }

    /**
     * Finds the maximum standard deviation vector - This looks at each standard
     * deviation found earlier and picks the largest value for each band to be
     * the max StDevVec.
     * 
     * @param StDevCluster
     *            - 2-dimensional array of standard deviation vectors found
     *            earlier
     * @return - the max standard deviation vector
     */
    public double[] FindStDevClusterMax(double[][] StDevCluster) {
        if (StDevCluster != null) {
            double[] StDevClusterMax = new double[StDevCluster[0].length];
            int i, j = 0;
            double max = 0;
            for (i = 0; i < StDevCluster[0].length; i++) {
                max = 0;
                for (j = 0; j < StDevCluster.length; j++) {
                    if (max < StDevCluster[j][i]) {
                        max = StDevCluster[j][i];
                    }
                }
                StDevClusterMax[i] = max;
            }
            return StDevClusterMax;
        } else {
            return null;
        }
    }

} //end UnsupervisedClustering{}
