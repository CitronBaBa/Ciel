import java.util.*;
import java.io.*;

// this class is a legacy from the last assignment
// Therefore some functions are not used

/* Do the job of saving tables to a folder with individual table file
   using Object/Object[] to make this class independent
   from the storing type, making it more reusable
*/

public class FileSystem
{   private String root;

    public FileSystem(String root)
    {   this.root = root;
    }

// save a list of object with each individual file name
// could be improved by using a map instead of two lists
    public void saveGroupObjects(String foldername, ArrayList<Object> objects, ArrayList<String> tablenames)
    {   File dir = new File(root+foldername);
        dir.mkdir();
        for (int i=0;i<objects.size();i++)
        {   writeObjectTo(foldername+"/"+tablenames.get(i),objects.get(i));
        }
        // delete file that is not in the lists
        for(File f : dir.listFiles())
        {   if(!tablenames.contains(f.getName()))
            f.delete();
        }
    }

// read from a folder(a Group) and return the list of objects (tables) read in
    public Object[] readGroupObjects(String foldername)
    {   File dir = new File(root+foldername);
        if(!dir.exists()) return null;
        if(foldername.equals("")) return null;
        File[] files = dir.listFiles();
        Object[] result = new Object[files.length];
        for(int i=0;i<files.length;i++)
        {   result[i] = readObjectFrom(foldername+"/"+files[i].getName());
        }
        return result;
    }

// delete a folder and all its inner file
    public void deleteGroup(String foldername)
    {   File dir = new File(root+foldername);
        for(File f : dir.listFiles())
        {   f.delete();
        }
        dir.delete();
    }

// read from a file, return the FILE
   public FileInputStream readFileFrom(String filename)
    {   FileInputStream file0 = null;
        try
        {   file0 = new FileInputStream(root+filename);
        }
        catch (Exception ex)
        {   System.out.println("ReadFileFrom Exception: " + ex);
        }
        return file0;
    }

// read from a file, return the object read in
    public Object readObjectFrom(String filename)
    {   FileInputStream file0 = null;
        Object targetobject = null;
        try
        {   file0 = new FileInputStream(root+filename);
            ObjectInputStream output = new ObjectInputStream(file0);

            targetobject = output.readObject();

            output.close();
        }
        catch (Exception ex)
        {   System.out.println("ReadObjectFrom Exception: " + ex);
        }
        finally
        {   try{  if (file0!= null) file0.close();}
            catch (IOException e) { System.out.println(" ExitError "+e);}
        }
        return targetobject;
     }

// write an object to a file
    public void writeObjectTo(String filename, Object targetobject)
    {   FileOutputStream file0 = null;
        try
        {   File newfile = new File(root+filename);
            newfile.createNewFile();
            file0 = new FileOutputStream(newfile);
            ObjectOutputStream output = new ObjectOutputStream(file0);
            output.writeObject(targetobject);
            output.close();
            file0.flush();
        }
        catch (Exception ex)
        {   System.out.println(" WriteObjectTo Exception: " + ex);
        }
        finally
        {   try{  if (file0!= null) file0.close();}
            catch (IOException e) { System.out.println("Exiterror"+e);}
        }
     }

    public static void main(String[] args)
    {   FileSystem handler = new FileSystem("./resources/");
        handler.test();
        System.out.println("test passed");
    }

    private void test()
    {   groupTest();
        singleTest();
    }

// test write and read a single object
    private void singleTest()
    {   Testclass cecile = new Testclass("Cecile");
        writeObjectTo("file2",cecile);
        Testclass cecilecopy = (Testclass)readObjectFrom("file2");
        assert(cecilecopy.name.equals("Cecile"));
        assert(cecilecopy.subclass.name.equals("inteli"));
        File testfile = new File(root+"file2");
        testfile.delete();
    }

// test write/read a group of objects
    private void groupTest()
    {   Testclass[] listofTestclass = testPreparation();
        assert(listofTestclass[0].name.equals("Cecile"));
        assert(listofTestclass[1].name.equals("Daisy"));
        assert(listofTestclass[0].subclass.name.equals("inteli"));
        deleteGroup("testDbFile");
    }

    private Testclass[] testPreparation()
    {   Testclass cecile = new Testclass("Cecile");
        Testclass daisy = new Testclass("Daisy");
        ArrayList<Object> people = new ArrayList<>();
        people.add(cecile); people.add(daisy);
        ArrayList<String> names = new ArrayList<>();
        names.add("testfile1");names.add("testfile2");

        saveGroupObjects("testDbFile",people,names);
        Object[] result = readGroupObjects("testDbFile");
        assert(result!=null);
        Testclass[] listofpeople = new Testclass[result.length];
        for(int i=0;i<listofpeople.length;i++)
        {   listofpeople[i] = (Testclass)result[i];
        }
        assert(listofpeople!=null);
        return listofpeople;
    }

// classes for testing
// each time they are created and deleted, no need to specify serialVersionUID
    static class Testclass implements Serializable
    {   public String name;
        public FileSystem.Testsubclass subclass = new FileSystem.Testsubclass();
        Testclass(String name)
        {   this.name = name;
        }
    }
    static class Testsubclass implements Serializable
    {   public String name = "inteli";
    }

}
