/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.scene.Node;
import java.util.ArrayList;

/**
 * 
 * @author capdevon
 */
public class Weapon {

	private String name;
	private Node weaponHook;
	private Node model;

	CrosshairData crosshair;
	float distance = 30f;
	float damage = 50f;
	int ammo = 20;

	private int ammoTypeIndex = 0;
	private final ArrayList<String> ammoTypes;

	/**
	 * Create new Weapon
	 * 
	 * @param name
	 * @param weaponHook
	 * @param model
	 */
	public Weapon(String name, Node weaponHook, Node model) {
		this.weaponHook = weaponHook;
		this.model = model;
		this.name = name;
		this.ammoTypes = new ArrayList<>();
	}

	public void setActive(boolean active) {
		if (active) {
			weaponHook.attachChild(model);
		} else {
			weaponHook.detachChild(model);
		}
	}

	public void onChangeAmmo() {
		ammoTypeIndex = (ammoTypeIndex + 1) % ammoTypes.size();
		System.out.println("AmmoType: " + ammoTypeIndex);
	}

	public String getAmmoType() {
		return ammoTypes.get(ammoTypeIndex);
	}

	public void addAmmoType(String model) {
		ammoTypes.add(model);
	}
}
