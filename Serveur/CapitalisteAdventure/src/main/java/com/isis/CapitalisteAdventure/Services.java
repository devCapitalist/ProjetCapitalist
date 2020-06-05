package com.isis.CapitalisteAdventure;

import com.isis.CapitalisteAdventure.generated.PallierType;
import com.isis.CapitalisteAdventure.generated.ProductType;
import com.isis.CapitalisteAdventure.generated.ProductsType;
import com.isis.CapitalisteAdventure.generated.World;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

public class Services {
    public World readWorldFromXml(String username) throws JAXBException{
        InputStream input = null;
        String file = "";
        JAXBContext jaxbContext;
        Unmarshaller u;
        World w;

        if (username != null){
            file = username + "-" + "world.xml";
            try{
                input = new FileInputStream(file);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (input == null){
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
        }

        if (input == null) {
            return null;
        }

        try{
            jaxbContext = JAXBContext.newInstance(World.class);
            u = jaxbContext.createUnmarshaller();
            w=(World) u.unmarshal(input);
            input.close();
            return w;
        }
        catch(JAXBException | IOException e) {
            e.printStackTrace();
        }
        return null;


        /*getClass().getClassLoader().getResourceAsStream("world.xml");
        JAXBContext j = JAXBContext.newInstance(World.class);
        Unmarshaller u = j.createUnmarshaller();
        return (World) u.unmarshal(input);*/
    }

    public void saveWorldToXml(String Username, World world) throws JAXBException, FileNotFoundException{
        OutputStream output;
        JAXBContext j;
        Marshaller m;

        if (Username==null)
            return;

        String filename= Username + "-" + "world.xml";

        try{
            output = new FileOutputStream((filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            j=JAXBContext.newInstance(World.class);
            m=j.createMarshaller();
            m.marshal(world, output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Marshaller marsh = JAXBContext.newInstance(World.class).createMarshaller();
        marsh.marshal(world, new FileOutputStream("world.xml"));*/

    }

    public World getWorld(String username) throws JAXBException {
        if (username == null)
            username="";
        World world =readWorldFromXml(username);
        return readWorldFromXml(username);
    }

    public ProductType findProductById(World w, int Id){
         return w.getProducts().getProduct().get(Id);
    }

    public PallierType findManagerByName(World w, String name) {
        return w.getManagers().getPallier().get(w.getManagers().getPallier().indexOf(name));

    }

    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException, FileNotFoundException {
        World world = getWorld(username);
        ProductType product = findProductById(world, newproduct.getId()) ;
        if (product == null) { return false;}

        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            world.setMoney(world.getMoney()-qtchange*newproduct.getCout());
        } else {
            product.setTimeleft(newproduct.getVitesse());
        }
        // sauvegarder les changements du monde
        saveWorldToXml(username, world);
        return true;
    }



    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException, FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }

        // débloquer ce manager
        manager.setUnlocked(true);

        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        newmanager.setUnlocked(true);

        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney()-newmanager.getSeuil());

        // sauvegarder les changements au monde
        saveWorldToXml(username, world);
        return true;
    }




}
