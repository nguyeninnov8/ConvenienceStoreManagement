/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data_objects;

import business_object.Product;
import business_object.TypeOfProduct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.IValidation;
import utils.MyUtils;
import utils.Status;
import utils.Validation;


public class ProductDao implements IProductDao{
    private final String PRODUCT_FILE_PATH = "D:\\FPT University\\Fall 2023\\LAB211\\Source Code\\J1.L.P0025.StoreManagementAtConvenienceStore\\src\\Product.dat";
    private List<Product> products;
    private IValidation validator = new Validation();
    
    public ProductDao() {
        this.products = new ArrayList<Product>();
    }

    @Override
    public List<Product> getAllProducts() {
        return products;
    }

    @Override
    public Product getProduct(String productCode) {
        for(Product prd: products) {
            if(prd.getCode().equalsIgnoreCase(productCode)) {
                return prd;
            }
        }
        return null;
    }

    @Override
    public void updateProduct(Product product) {
        Product updatedProduct = getProduct(product.getCode());
        updatedProduct = product;
        System.out.println("Product: ID " + updatedProduct.getCode() + "has been updated to the Database;");
    }

    @Override
    public void deleteProduct(Product product) {
        products.remove(product);
        System.out.println("Product: ID " + product.getCode() + "has been deleted to the Database;");
    }

    @Override
    public void addProduct() {
        loadProductFromFile(PRODUCT_FILE_PATH);
        
        String productCode = "";
        
        do {
        try {
            SimpleDateFormat dtobj = new SimpleDateFormat("dd/MM/yyyy");
            productCode = validator.checkProductCodeExist("Input product code: ", products, Status.UPDATE);
           
            String productName = validator.inputString("Input product name", Status.UPDATE);
            double price = validator.checkDouble("Input product price: ", 1.0, Double.MAX_VALUE);
            int quantity = validator.checkInt("Enter product quantity", 1, Integer.MAX_VALUE);
            TypeOfProduct type = TypeOfProduct.valueOf(validator.checkType("Enter product Type (DAILY & LONG):", Status.UPDATE));
            String strManufacturingDate = validator.inputString("Enter manufacturing date: ", Status.UPDATE);
            String strExpiredDate = validator.inputString("Enter expired date: ", Status.UPDATE);
            
            Date manufacturingDate = dtobj.parse(strManufacturingDate);
            Date expiredDate = dtobj.parse(strExpiredDate);
            
            Product addedProduct = new Product(productCode, productName, price, quantity,type, manufacturingDate, expiredDate);
            products.add(addedProduct);
            System.out.println("Product: ID " + addedProduct.getCode() + " has been added to the Database;");
            saveProductsToFile(PRODUCT_FILE_PATH);
        } catch (ParseException ex) {
            System.err.println("Please enter correct format \"dd/MM/yyyy\"");
        }
        } while(validator.checkYesOrNo("Do you want to continue adding product? Press \"Y\" or \"N\""));
    }

    @Override
    public List<Product> getExpiredProduct() {
        List<Product> expiredProducts = new ArrayList<>();
        Date now = new Date();
        for (Product product : products) {
            if(now.after(product.getExpiredDate())) {
                expiredProducts.add(product);
            }
        }
        return expiredProducts;
    }

    @Override
    public List<Product> getSellingProduct() {
        List<Product> sellingProducts = new ArrayList<>();
        for(Product product: products) {
            if(product.getManufacturingDate().before(product.getExpiredDate()) && product.getQuantity() > 0) {
                sellingProducts.add(product);
            }
        }
        return sellingProducts;
    }

    @Override
    public List<Product> getOutOfStockProduct() {
        List<Product> outOfStockProducts = new ArrayList<>();
        for(Product product: products) {
            if(product.getQuantity() < 3) {
                outOfStockProducts.add(product);
            }
        }
        return outOfStockProducts;
    }
    
    @Override
    public boolean saveProductsToFile(String filePath) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        try {
            PrintWriter printWriter = new PrintWriter(filePath);
            for(Product product: products) {
                printWriter.println(product.getCode() + ", " + product.getName() + ", " + product.getPrice()+ ", " + product.getQuantity()+ ", " + product.getType() + ", " + dateFormat.format(product.getManufacturingDate())+ ", " + dateFormat.format(product.getExpiredDate()));
            }
            printWriter.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProductDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    
    @Override
    public boolean loadProductFromFile(String filePath) {
        File file = new File(filePath);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        if(!file.exists()) {
            return false;
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = "";
                while((line = reader.readLine()) != null) {
                        String[] rows = line.split(",");
                        String productCode = rows[0].trim();
                        String productName = rows[1].trim();
                        double price = Double.parseDouble(rows[2].trim());
                        int quantity = Integer.parseInt(rows[3].trim());
                        TypeOfProduct type = TypeOfProduct.valueOf(rows[4].trim());
                        Date manufacturingDate = dateFormat.parse(rows[5].trim());
                        Date expiredDate = dateFormat.parse(rows[6].trim());
                        
                        Product productFromFile = new Product(productCode, productName, price, quantity, type, manufacturingDate, expiredDate);
                        products.add(productFromFile);
                    }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProductDao.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
        return true;
    }
}
