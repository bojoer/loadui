/* 
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.fx.util;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Resizable;

import java.awt.image.BufferedImage;
import java.applet.Applet;
import java.awt.Frame;
import javax.swing.JFrame;
import javafx.geometry.BoundingBox;
import javafx.reflect.FXLocal;
import javafx.scene.layout.Container;
import java.awt.Graphics2D;
import java.io.File;
import java.lang.Void;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import org.apache.commons.codec.binary.Base64;
import javafx.util.Math;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import javafx.ext.swing.SwingUtils;
import com.sun.scenario.scenegraph.SGNode;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.lang.Exception;

def context = FXLocal.getContext();
def nodeClass = context.findClass("javafx.scene.Node");
def getFXNode = nodeClass.getFunction("impl_getPGNode");

public function nodeToImage(node: Node) : BufferedImage {
	def nodeBounds = node.layoutBounds;
	nodeToImage(node, nodeBounds.width, nodeBounds.height);
}

public function nodeToImage(node: Node, width: Number, height: Number) : BufferedImage {

    var g2:Graphics2D;

    def sgNode = (getFXNode.invoke(
        context.mirrorOf(node)) as FXLocal.ObjectValue).asObject();
    def g2dClass = (context.findClass(
        "java.awt.Graphics2D") as FXLocal.ClassType).getJavaImplementationClass();
    def boundsClass = (context.findClass(
        "com.sun.javafx.geom.Bounds2D")
            as FXLocal.ClassType).getJavaImplementationClass();
    def affineClass = (context.findClass(
        "com.sun.javafx.geom.transform.BaseTransform")
            as FXLocal.ClassType).getJavaImplementationClass();

    def getBounds = sgNode.getClass().getMethod(
        "getContentBounds", boundsClass, affineClass);
    def bounds2D = getBounds.invoke(
        sgNode, new com.sun.javafx.geom.Bounds2D(),
            new com.sun.javafx.geom.transform.Affine2D());

    var paintMethod = sgNode.getClass().getMethod(
        "render", g2dClass, boundsClass, affineClass);

    var ge: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
    var gs: GraphicsDevice = ge.getDefaultScreenDevice(); 
    var gc: GraphicsConfiguration = gs.getDefaultConfiguration(); 
    def bufferedImage = gc.createCompatibleImage(width, height, Transparency.BITMASK);

    g2 = (bufferedImage.getGraphics() as Graphics2D);
    paintMethod.invoke(sgNode, g2, bounds2D,
        new com.sun.javafx.geom.transform.Affine2D());
    g2.dispose();

    return bufferedImage;
}

public function saveImage(image: BufferedImage, path: String) : Void {
	saveImage(image, new File(path));
}

public function saveImage(image: BufferedImage, file : File) : Void {
    if(file == null) { return; }
    ImageIO.write(image, "png", file);
}

public function bufferedImageToBase64(image: BufferedImage): String {
    var baos: ByteArrayOutputStream = new ByteArrayOutputStream();
	ImageIO.write(image, "png", baos);
	Base64{}.encodeToString(baos.toByteArray());
}

public function base64ToBufferedImage(base64: String): BufferedImage {
	var bis: ByteArrayInputStream = new ByteArrayInputStream(Base64{}.decode(base64));
	try{
		return ImageIO.read(bis);
	}
	catch(e: Exception){
		return null;
	}
}

public function bufferedToFXImage(buffered: BufferedImage): Image {
	SwingUtils.toFXImage(buffered);
}

public function base64ToFXImage(base64: String): Image {
	var img = base64ToBufferedImage(base64);
	if(img != null){
		return bufferedToFXImage(img);
	}
	else{
		return null;
	}
}

public function scaleImage(image: BufferedImage, maxWidth: Number, maxHeight: Number): BufferedImage {
	
	def imageWidth: Number = image.getWidth();
	def imageHeight: Number = image.getHeight();
	var scale = Math.min( maxWidth / imageWidth, maxHeight / imageHeight );
	
	def width: Number = imageWidth * scale;
	def height: Number = imageHeight * scale;
	
	var scaledImage: BufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	
	var graphics2D: Graphics2D = scaledImage.createGraphics();
	graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	graphics2D.drawImage(image, 0, 0, width, height, null);
	graphics2D.dispose();
	
	scaledImage;		
}

public function scaleImage(image: BufferedImage, scale: Number): BufferedImage {
	def width: Number = image.getWidth() * scale;
	def height: Number = image.getHeight() * scale;
	
	var scaledImage: BufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	
	var graphics2D: Graphics2D = scaledImage.createGraphics();
	graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	graphics2D.drawImage(image, 0, 0, width, height, null);
	graphics2D.dispose();
	scaledImage;		
}

public function clipImage(image: BufferedImage, x: Number, y: Number, w: Number, h: Number): BufferedImage {
	image.getSubimage(x, y, w, h); 
}

public function combineImages(firstImage: BufferedImage, secondImage: BufferedImage): BufferedImage {
	var width: Number = Math.max(firstImage.getWidth(), secondImage.getWidth()); 
	var height: Number = Math.max(firstImage.getHeight(), secondImage.getHeight());
	var combined: BufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 
	var g: Graphics2D = combined.createGraphics();
	g.drawImage(firstImage, 0, 0, width, height, null);
	g.drawImage(secondImage, 0, 0, width, height, null);
	g.dispose();
 
	combined;
}

public function createTransparentImage(width: Number, height: Number): BufferedImage {
	new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
} 

public function addLayerToImage(baseImage: BufferedImage, layerImage: BufferedImage, offsetX: Number, offsetY: Number): BufferedImage {
	var g: Graphics2D = baseImage.createGraphics();
	g.drawImage(layerImage, offsetX, offsetY, layerImage.getWidth(), layerImage.getHeight(), null);
	g.dispose();
	baseImage;
}
	