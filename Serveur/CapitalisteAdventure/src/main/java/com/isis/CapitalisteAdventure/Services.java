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
import java.util.ArrayList;
import java.util.List;

public class Services {
    public World readWorldFromXml(String username) {
        InputStream input = null;
        String file = "";
        JAXBContext jaxbContext;
        Unmarshaller u;
        World w;

        if (username != null) {
            file = username + "-" + "world.xml";

            try {
                input = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                System.out.println(file);
                e.printStackTrace();
            }
        }

        if (input == null) {
            input = getClass().getClassLoader().getResourceAsStream("world.xml");

        }

        if (input == null) {
            return null;
        }

        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            u = jaxbContext.createUnmarshaller();
            w = (World) u.unmarshal(input);
            input.close();
            return w;
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void saveWorldToXml(String Username, World world) throws JAXBException {
        OutputStream output;
        JAXBContext j;
        Marshaller m;

        if (Username == null)
            return;

        String filename = Username + "-" + "world.xml";

        try {
            output = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            j = JAXBContext.newInstance(World.class);
            m = j.createMarshaller();
            m.marshal(world, output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public World getWorld(String username) {
        if (username == null)
            username = "";

        World world = readWorldFromXml(username);
        if (world.getLastupdate() == 0) {
            world.setLastupdate(System.currentTimeMillis());
        }

        updateScore(world);

        return world;
    }

    private void updateScore(World world) {
        long tempsActuel = System.currentTimeMillis();
        long tempsEcoule = tempsActuel - world.getLastupdate();
        long tempsRestant;
        int qtProduit;
        double gain;
        world.setLastupdate(tempsActuel);
        for (ProductType p : world.getProducts().getProduct()) {
            tempsRestant = tempsEcoule - p.getTimeleft();
            if (tempsRestant < 0)
                p.setTimeleft(p.getTimeleft() - tempsEcoule);
            else {
                qtProduit = 0;
                if (p.isManagerUnlocked()) {
                    qtProduit = ((int) tempsRestant / p.getVitesse()) + 1;
                    p.setTimeleft((p.getVitesse() - tempsRestant % p.getVitesse()));
                } else if (p.getTimeleft() > 0) {
                    qtProduit = 1;
                    p.setTimeleft(0);
                }
                gain = qtProduit * p.getQuantite() * p.getRevenu();
                gain += gain * (world.getActiveangels() * world.getAngelbonus() / 100);
                world.setMoney(world.getMoney() + gain);
                world.setScore(world.getScore() + gain);

            }
        }
    }

    public ProductType findProductById(World w, int Id) {
        for (ProductType p : w.getProducts().getProduct()) {
            if (p.getId() == Id)
                return p;
        }
        return null;
    }

    public PallierType findManagerByName(World w, String name) {
        for (PallierType p : w.getManagers().getPallier()) {
            if (p.getName() == name)
                return p;
        }
        return null;
    }

    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException {

        World world = getWorld(username);
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null || newproduct.getQuantite() < 0) {
            return false;
        }

        int qtchange = newproduct.getQuantite() - product.getQuantite();


        if (qtchange > 0) {

            double achatTotal = product.getCout() *
                    (1 - Math.pow(product.getCroissance(), qtchange)) /
                    (1 - product.getCroissance());

            if (achatTotal > world.getMoney())
                return false;


            world.setMoney(world.getMoney() - achatTotal);
            product.setQuantite(newproduct.getQuantite());

            // Application des unlocks du produit
            for (PallierType p : product.getPalliers().getPallier()) {
                if (!p.isUnlocked())
                    calculeUpgrade(p, product);
            }

            List<ProductType> Listproduct = world.getProducts().getProduct();
            int qtMinimum = Listproduct.get(0).getQuantite();
            for (ProductType p : Listproduct) {
                if (p.getQuantite() < qtMinimum)
                    qtMinimum = p.getQuantite();
            }
            for (PallierType pallierType : world.getAllunlocks().getPallier()) {
                if (!pallierType.isUnlocked() && pallierType.getSeuil() < qtMinimum)
                    calculeUpgrade(pallierType, product);
            }

        } else {
            if (product.getTimeleft() <= 0)
                product.setTimeleft(product.getVitesse());
        }

        // sauvegarder les changements du monde
        saveWorldToXml(username, world);
        return true;
    }

    private void calculeUpgrade(PallierType pallierType, ProductType product) {
        if (pallierType.getSeuil() <= product.getQuantite()) {
            if (pallierType.getTyperatio().value() == "vitesse") {
                product.setVitesse((int) (product.getVitesse() / pallierType.getRatio()));
                product.setTimeleft((int) (product.getTimeleft() / pallierType.getRatio()));
            } else if (pallierType.getTyperatio().value() == "gain")
                product.setRevenu(product.getRevenu() * pallierType.getRatio());

            pallierType.setUnlocked(true);
        }
    }

    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException {
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
        product.setManagerUnlocked(true);
        //newmanager.setUnlocked(true);

        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney() - newmanager.getSeuil());

        // sauvegarder les changements au monde
        saveWorldToXml(username, world);
        return true;
    }

    public boolean resetWorld(String username) throws JAXBException {
        World w = getWorld(username);
        double angelDemande =
                Math.round(150 * Math.sqrt(w.getScore()
                        / Math.pow(10, 15))) - w.getTotalangels();
        if (angelDemande < 0)
            angelDemande = 0;
        double angelTotal = angelDemande + w.getTotalangels();
        double angelActive = w.getActiveangels() + angelDemande;

        World nouveauMonde = readWorldFromXml(null);
        nouveauMonde.setTotalangels(angelTotal);
        nouveauMonde.setActiveangels(angelActive);
        nouveauMonde.setScore(w.getScore());

        saveWorldToXml(username, nouveauMonde);

        return true;
    }


    public boolean updateUpgrade(String username, PallierType newupgrade, String type) throws JAXBException {

        World world = getWorld(username);
        PallierType upgrade = world.getUpgrades().getPallier(newupgrade.getName());
        if (upgrade == null || upgrade.isUnlocked())
            return false;

        List<ProductType> listProduct = new ArrayList<>();
        if (upgrade.getIdcible() > 0) {
            ProductType product = world.getProducts().getProduct(newupgrade.getIdcible());
            if (product == null)
                return false;
            listProduct.add(product);
        } else if (upgrade.getIdcible() == 0) {
            listProduct = world.getProducts().getProduct();
        } else if (upgrade.getIdcible() < -1) {
            return false;
        }

        if (upgrade.getSeuil() > world.getMoney())
            return false;
        world.setMoney(world.getMoney() - upgrade.getSeuil());

        if (upgrade.getTyperatio().value() == "gain") {
            for (ProductType p : listProduct) {
                p.setRevenu(p.getRevenu() * upgrade.getRatio());
            }
        } else if (upgrade.getTyperatio().value() == "vitesse") {
            if (listProduct == null)
                return false;
            for (ProductType p : listProduct) {
                p.setVitesse((int) (p.getVitesse() / upgrade.getRatio()));
                p.setTimeleft((int) (p.getTimeleft() / upgrade.getRatio()));
            }
        } else if (upgrade.getTyperatio().value() == "ange")
            world.setAngelbonus((int) (world.getAngelbonus() + upgrade.getRatio()));
        else
            return false;


        upgrade.setUnlocked(true);

        saveWorldToXml(username, world);
        return true;
    }

    public boolean updateAngelUpgrade(String username, PallierType newupgrade) throws JAXBException {
        World world = getWorld(username);
        PallierType upgrade = world.getAngelupgrades().getPallier(newupgrade.getName());
        if (upgrade == null)
            return false;
        if (upgrade.isUnlocked())
            return false;

        List<ProductType> listProduct = new ArrayList<>();
        if (upgrade.getIdcible() > 0 || upgrade.getIdcible() < -1) {
            return false;
        } else if (upgrade.getIdcible() == 0) {
            listProduct = world.getProducts().getProduct();
        }

        if (upgrade.getSeuil() > world.getActiveangels())
            return false;
        world.setActiveangels(world.getActiveangels() - upgrade.getSeuil());

        if (upgrade.getTyperatio().value() == "gain") {
            if (listProduct == null) return false;
            for (ProductType p : listProduct) {
                p.setRevenu(p.getRevenu() * upgrade.getRatio());
            }
        } else if (upgrade.getTyperatio().value() == "vitesse") {
            if (listProduct == null) return false;
            for (ProductType p : listProduct) {
                p.setVitesse((int) (p.getVitesse() / upgrade.getRatio()));
                p.setTimeleft((int) (p.getTimeleft() / upgrade.getRatio()));
            }
        } else if (upgrade.getTyperatio().value() == "ange")
            world.setAngelbonus((int) (world.getAngelbonus() + upgrade.getRatio()));
        else
            return false;

        upgrade.setUnlocked(true);

        saveWorldToXml(username, world);
        return true;
    }
}
