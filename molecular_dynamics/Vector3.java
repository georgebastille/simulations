//  Uncommented Vector class.  Beautify, comment and comprehend
import java.io.*;
class Vector3 {
    double x,y,z;     double modulus;   String print;
    public Vector3(double ix, double iy, double iz)
                  {this.x = ix;this.y = iy;this.z = iz;} 
void setX (double x) {  this.x = x;  }
void setY (double y) {  this.y = y;  }
void setZ (double z) {  this.z = z;  }
double getX () { return this.x;  }
double getY () { return this.y;  }
double getZ () { return this.z;  }
void setModulus () {  this.modulus = Math.sqrt(x*x + y*y + z*z);  }
double getModulus () {  return Math.sqrt(x*x + y*y + z*z);  }
public static double dot(Vector3 a, Vector3 b) {
 double  c = a.x*b.x + a.y*b.y + a.z*b.z;
        return (c); }
public double dot(Vector3 b) {
 double  c = this.x*b.x + this.y*b.y + this.z*b.z;
        return (c); }
public static double angle(Vector3 a, Vector3 b) {
    a.setModulus();    b.setModulus();
    double  c = Vector3.dot(a,b)/a.getModulus()/b.getModulus() ;
     if(c>1.0) c=1.0;
     else if(c<-1.0) c=-1.0;
     c = Math.acos(c) ;
        return (c); }
 public static Vector3 cross(Vector3 a, Vector3 b) {
        Vector3 c = new Vector3(0,0,0)  ;
 c.x = a.y*b.z - a.z*b.y; c.y = a.z*b.x - a.x*b.z; c.z = a.x*b.y - a.y*b.x;
        return (c); }
 public Vector3 cross(Vector3 b) {
        Vector3 c = new Vector3(0,0,0)  ;
 c.x = this.y*b.z - this.z*b.y; c.y = this.z*b.x - this.x*b.z; c.z = this.x*b.y - this.y*b.x;
        return (c); }
 public static Vector3 add(Vector3 a, Vector3 b) {
        Vector3 c = new Vector3(0,0,0);
c.x = (a.x+b.x);  c.y = (a.y+b.y);  c.z = (a.z+b.z);  
        return (c);
 }
 public static Vector3 subtract(Vector3 a, Vector3 b) {
        Vector3 c = new Vector3(0,0,0);
        c.x = (a.x-b.x);  c.y = (a.y-b.y);   c.z = (a.z-b.z);  
        return (c);
 }
 public Vector3 subtract(Vector3 b) {
        return (new Vector3(this.x-b.x,this.y-b.y,this.z-b.z));
 }
 public static Vector3 multiply(Vector3 a, double b) {
        Vector3 c = new Vector3(0,0,0);
c.x = a.x*b;          c.y = a.y*b;          c.z = a.z*b;  
        return (c); }
 public Vector3 multiply(double b) {
        Vector3 c = new Vector3(0,0,0);
c.x = this.x*b;          c.y = this.y*b;          c.z = this.z*b;  
        return (c); }

public  String toString () {
    String string = "(" + x + ", " + y + ", " + z + ")" ;
      return (string);      
   }
void printString () {
   this.print = "(" + x + ", " + y + ", " + z + ")" ;
      System.out.println(this.print);      
}}
