package edu.wlu.cs.levy.CG;

import java.util.Vector;

/**
 * KDTree is a class supporting KD-tree insertion, deletion, equality search,
 * range search, and nearest neighbor(s) using double-precision floating-point
 * keys. Splitting dimension is chosen naively, by depth modulo K. Semantics are
 * as follows:
 * 
 * <UL>
 * <LI>Two different keys containing identical numbers should retrieve the same
 * value from a given KD-tree. Therefore keys are cloned when a node is
 * inserted. <BR>
 * <BR>
 * <LI>As with Hashtables, values inserted into a KD-tree are <I>not</I> cloned.
 * Modifying a value between insertion and retrieval will therefore modify the
 * value stored in the tree.
 *</UL>
 * 
 * @author Simon Levy, Bjoern Heckel
 * @version %I%, %G%
 * @since JDK1.2
 */
public class KDTree {

    // K = number of dimensions
    private final int m_K;

    // root of KD-tree
    private KDNode    m_root;

    // count of nodes
    private int       m_count;

    // ----------------------------------------------------------------------
    // START James Rapp
    // ----------------------------------------------------------------------
    //stores the euclidean distances between the first node returned by
    //nearest(double[], int) and all subsequent nodes respectively
    private double[]  distances = null;

    // ----------------------------------------------------------------------
    // END James Rapp
    // ----------------------------------------------------------------------

    /**
     * Creates a KD-tree with specified number of dimensions.
     * 
     * @param k
     *            number of dimensions
     */
    public KDTree(int k) {

        m_K = k;
        m_root = null;
    }

    /**
     * Insert a node in a KD-tree. Uses algorithm translated from 352.ins.c of
     * 
     * <PRE>
     *   &#064;Book{GonnetBaezaYates1991,                                   
     *     author =    {G.H. Gonnet and R. Baeza-Yates},
     *     title =     {Handbook of Algorithms and Data Structures},
     *     publisher = {Addison-Wesley},
     *     year =      {1991}
     *   }
     * </PRE>
     * 
     * @param key
     *            key for KD-tree node
     * @param value
     *            value at that key
     * 
     * @throws KeySizeException
     *             if key.length mismatches K
     * @throws KeyDuplicateException
     *             if key already in tree
     */
    public void insert(double[] key, Object value) throws KeySizeException,
            KeyDuplicateException {

        if (key.length != m_K) {
            throw new KeySizeException();
        } else {
            try {
                m_root = KDNode.ins(new HPoint(key), value, m_root, 0, m_K);
            }

            catch (KeyDuplicateException e) {
                throw e;
            }
        }

        m_count++;
    }

    /**
     * Find KD-tree node whose key is identical to key. Uses algorithm
     * translated from 352.srch.c of Gonnet & Baeza-Yates.
     * 
     * @param key
     *            key for KD-tree node
     * 
     * @return object at key, or null if not found
     * 
     * @throws KeySizeException
     *             if key.length mismatches K
     */
    public Object search(double[] key) throws KeySizeException {

        if (key.length != m_K) {
            throw new KeySizeException();
        }

        KDNode kd = KDNode.srch(new HPoint(key), m_root, m_K);

        return (kd == null ? null : kd.v);
    }

    /**
     * Delete a node from a KD-tree. Instead of actually deleting node and
     * rebuilding tree, marks node as deleted. Hence, it is up to the caller to
     * rebuild the tree as needed for efficiency.
     * 
     * @param key
     *            key for KD-tree node
     * 
     * @throws KeySizeException
     *             if key.length mismatches K
     * @throws KeyMissingException
     *             if no node in tree has key
     */
    public void delete(double[] key) throws KeySizeException,
            KeyMissingException {

        if (key.length != m_K) {
            throw new KeySizeException();
        }

        else {

            KDNode t = KDNode.srch(new HPoint(key), m_root, m_K);
            if (t == null) {
                throw new KeyMissingException();
            } else {
                t.deleted = true;
            }

            m_count--;
        }
    }

    /**
     * Find KD-tree node whose key is nearest neighbor to key. Implements the
     * Nearest Neighbor algorithm (Table 6.4) of
     * 
     * <PRE>
     * &#064;techreport{AndrewMooreNearestNeighbor,
     *   author  = {Andrew Moore},
     *   title   = {An introductory tutorial on kd-trees},
     *   institution = {Robotics Institute, Carnegie Mellon University},
     *   year    = {1991},
     *   number  = {Technical Report No. 209, Computer Laboratory, 
     *              University of Cambridge},
     *   address = {Pittsburgh, PA}
     * }
     * </PRE>
     * 
     * @param key
     *            key for KD-tree node
     * 
     * @return object at node nearest to key, or null on failure
     * 
     * @throws KeySizeException
     *             if key.length mismatches K
     */
    public Object nearest(double[] key) throws KeySizeException {

        Object[] nbrs = nearest(key, 1);
        return nbrs[0];
    }

    /**
     * Find KD-tree nodes whose keys are <I>n</I> nearest neighbors to key. Uses
     * algorithm above. Neighbors are returned in ascending order of distance to
     * key.
     * 
     * @param key
     *            key for KD-tree node
     * @param n
     *            how many neighbors to find
     * 
     * @return objects at node nearest to key, or null on failure
     * 
     * @throws KeySizeException
     *             if key.length mismatches K
     * @throws IllegalArgumentException
     *             if <I>n</I> is negative or exceeds tree size
     */
    public Object[] nearest(double[] key, int n) throws KeySizeException,
            IllegalArgumentException {

        if (n < 0 || n > m_count) {
            throw new IllegalArgumentException("Number of neighbors cannot"
                    + " be negative or greater than number of nodes");
        }

        if (key.length != m_K) {
            throw new KeySizeException();
        }

        Object[] nbrs = new Object[n];
        NearestNeighborList nnl = new NearestNeighborList(n);

        // initial call is with infinite hyper-rectangle and max distance
        HRect hr = HRect.infiniteHRect(key.length);
        double max_dist_sqd = Double.MAX_VALUE;
        HPoint keyp = new HPoint(key);

        KDNode.nnbr(m_root, keyp, hr, max_dist_sqd, 0, m_K, nnl);

        // ----------------------------------------------------------------------
        // START James Rapp
        // ----------------------------------------------------------------------
        KDNode[] neighborsList = new KDNode[n];
        distances = new double[n - 1];//stores the distances between the first nearest neighbor
        //and all subsequent nearest neighbors respectively
        for (int i = 0; i < n; ++i) {//not my code
            KDNode kd = (KDNode) nnl.removeHighest();//not my code
            neighborsList[i] = kd;
            nbrs[n - i - 1] = kd.v;//not my code
        }
        //reverse order of neighbors list
        KDNode[] temp = new KDNode[n];
        for (int i = 0; i < n; ++i) {
            temp[i] = neighborsList[n - i - 1];
        }

        neighborsList = temp;

        for (int i = 1; i < n; ++i) {
            HPoint firstPoint = neighborsList[0].k;
            HPoint currPoint = neighborsList[i].k;
            distances[i - 1] = HPoint.eucdist(firstPoint, currPoint);
        }
        // ----------------------------------------------------------------------
        // END James Rapp
        // ----------------------------------------------------------------------

        return nbrs;
    }

    /**
     * Range search in a KD-tree. Uses algorithm translated from 352.range.c of
     * Gonnet & Baeza-Yates.
     * 
     * @param lowk
     *            lower-bounds for key
     * @param uppk
     *            upper-bounds for key
     * 
     * @return array of Objects whose keys fall in range [lowk,uppk]
     * 
     * @throws KeySizeException
     *             on mismatch among lowk.length, uppk.length, or K
     */
    public Object[] range(double[] lowk, double[] uppk) throws KeySizeException {

        if (lowk.length != uppk.length) {
            throw new KeySizeException();
        }

        else if (lowk.length != m_K) {
            throw new KeySizeException();
        }

        else {
            Vector<KDNode> v = new Vector<KDNode>();
            KDNode.rsearch(new HPoint(lowk), new HPoint(uppk), m_root, 0, m_K,
                    v);
            Object[] o = new Object[v.size()];
            for (int i = 0; i < v.size(); ++i) {
                KDNode n = v.elementAt(i);
                o[i] = n.v;
            }
            return o;
        }
    }

    @Override
    public String toString() {
        return m_root.toString(0);
    }

    // ----------------------------------------------------------------------
    // START James Rapp
    // ----------------------------------------------------------------------

    /**
     * Returns the distances between the first value in the array returned by
     * nearest(double[], int) and all subsequent values respectively (my code)
     */
    public double[] getDistances() throws Exception {
        if (distances == null) {
            throw new Exception(
                    "distances hasn't been set yet.  First call nearest(double[], int)");
        }

        return distances;
    }

    /**
     * Return an array of doubles where each index stores the distance between
     * the parameter key and the ith nearest neighbor to that key
     * 
     * @param key
     *            the key to which all nearest neighbor distances will be
     *            calculated
     * @param n
     *            the number of nearest neighbors we're interested in looking at
     * @return an array of doubles where each index stores the distance between
     *         the parameter key and the ith nearest neighbor to that key
     */
    public double[] getDistances(double[] key, int n) throws Exception {
        //ensure key has same number of dimensions as all the other keys in the tree
        if (key.length != m_K) {
            throw new Exception(
                    "Dimensions of parameter key must agree with the dimensions"
                            + " of this tree.");
        }
        //ensure n is positive
        if (n <= 0) {
            throw new Exception("n must be positive.");
        }

        //ensure n is less than or equal to the number of nodes in the tree
        if (n > m_count) {
            throw new Exception(
                    "n must be less than or equal to the number of nodes in this tree.");
        }

        double[] theDistances = new double[n];

        NearestNeighborList nnl = new NearestNeighborList(n);
        // initial call is with infinite hyper-rectangle and max distance
        HRect hr = HRect.infiniteHRect(key.length);
        double max_dist_sqd = Double.MAX_VALUE;
        HPoint keyp = new HPoint(key);

        KDNode.nnbr(m_root, keyp, hr, max_dist_sqd, 0, m_K, nnl);
        HPoint sourcePoint = new HPoint(key);
        for (int i = 0; i < n; i++) {
            KDNode kd = (KDNode) nnl.removeHighest();
            HPoint currPoint = kd.k;
            theDistances[i] = HPoint.eucdist(sourcePoint, currPoint);
        }

        //reverse order of distances array
        double[] temp = new double[theDistances.length];
        for (int i = 0; i < n; i++) {
            temp[i] = theDistances[n - i - 1];
        }
        theDistances = temp;
        return theDistances;
    }

    // ----------------------------------------------------------------------
    // END James Rapp
    // ----------------------------------------------------------------------
}
