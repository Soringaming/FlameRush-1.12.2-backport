package me.soringaming.fireshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireShot extends FireAbility implements AddonAbility, ComboAbility {
	
	public static Config config;
	
	List<Entity> hitEntities = new ArrayList<Entity>();
	
	private double range;
	private double damage;
	private double t;
	
	private Location origin;
	private Location loc;
	private Location effectLocation;
	private Location effectLocation2;
	private Location effectLocation3;
	private Location effectLocation4;
	private Vector dir;
	
	private Player p;
	
	private boolean hasFired;
	
	double phi;
	
	int flameAmount;
	private float radiusF;
	private double radius;

	public FireShot(Player player) {
		super(player);
		this.p = player;
		if(bPlayer.isOnCooldown(this)) {
			return;
		}
		if (hasAbility(p, FireShot.class)) {
			((FireShot) getAbility(p, FireShot.class)).remove();
			return;
		}
		setFields();
		start();
		bPlayer.addCooldown(this);
	}
	
	private void setFields() {
		this.range = config.get().getDouble("Combos.Fire.FlameRush.Range");
		this.damage = config.get().getDouble("Combos.Fire.FlameRush.Damage");
		this.origin = player.getEyeLocation().clone();
		this.loc = origin.clone();
		this.effectLocation = origin.clone();
		this.effectLocation2 = origin.clone();
		this.effectLocation3 = origin.clone();
		this.effectLocation4 = origin.clone();
		this.dir = player.getEyeLocation().getDirection().clone();
		this.hasFired = false;
		this.flameAmount = 5;
		this.radius = 0.05;
		this.radiusF = 0.05f;
		this.t = 0;
		
		this.phi = 0;
	}

	@Override
	public long getCooldown() {
		return config.get().getLong("Combos.Fire.FlameRush.Cooldown");
	}

	@Override
	public Location getLocation() {
		return loc;
	}

	@Override
	public String getName() {
		return "FlameRush";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public void progress() {
		if(p.isDead() || !p.isOnline()) {
			remove();
			return;
		}
		if(origin.distance(loc) > range) {
			remove();
			return;
		}
		if(!hasFired && origin.distance(loc) > range / 2) {
			remove();
			return;
		}
		if(!bPlayer.isOnCooldown(this)) {
			bPlayer.addCooldown(this);
		}
		this.dir = p.getEyeLocation().getDirection().clone();
		if(player.isSneaking() || hasFired) {
			if(!hasFired) {
				hasFired = true;
			}
			loc.add(dir.multiply(config.get().getDouble("Combos.Fire.FlameRush.Speed")));
			effectLocation = loc.clone();
			effectLocation2 = loc.clone();
			effectLocation3 = loc.clone();
			effectLocation4 = loc.clone();
		} else {
			loc = p.getEyeLocation();
			loc.add(dir.multiply(0.02));
			effectLocation = loc.clone();
			effectLocation2 = loc.clone();
			effectLocation3 = loc.clone();
			effectLocation4 = loc.clone();
		}
		if(hasFired) {
			newFlameEffect();
		} else {
			newFlameEffect2();
		}
		FireEffects();
		if(GeneralMethods.isSolid(loc.getBlock()) || isWater(loc.getBlock())) {
			remove();
			return;
		}
		
		for(Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1.5 + radius)) {
			if((e instanceof LivingEntity) && e.getUniqueId() != p.getUniqueId() && !hitEntities.contains(e)) {
				DamageHandler.damageEntity(e, damage, this);
				hitEntities.add(e);
			}
		}
		
		
	}
	
	@Override
	public Object createNewComboInstance(Player p) {
		return new FireShot(p);
	}
	
	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("FireBurst", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireBurst", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("FireBurst", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireBlast", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireBlast", ClickType.SHIFT_UP));
		return combo;
	}
	
	@Override
	public String getInstructions() {
		return "FireBurst (Hold Sneak) > FireBurst (Left Click) > FireBurst (Release Sneak) > FireBlast (Tap Sneak) *To fire simply tap sneak after holding for desired charge*";
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}
	
	private void FireEffects() {
		if(loc.distance(p.getLocation()) > 1 && flameAmount < 50 && !hasFired) {
			flameAmount += 2;
		}
		if(loc.distance(p.getLocation()) > 1 && radius < 1.8 && !hasFired) {
			radius += 0.1f;
			radiusF += 0.1f;
		} else if (loc.distance(p.getLocation()) > 4 && hasFired && radius < 1.4) {
			radius += 0.2f;
			radiusF += 0.2f;
		}
		if(!hasFired) {
			ParticleEffect.FLAME.display(loc, radiusF, radiusF, radiusF, radiusF / 40, flameAmount / 16);
		} else {
			ParticleEffect.FLAME.display(loc, radiusF / 6, radiusF / 6, radiusF / 6, radiusF / 40, flameAmount);
		}
		
		if(config.get().getBoolean("Combos.Fire.FlameRush.CreateFlames") && hasFired) {
			for(Block b : GeneralMethods.getBlocksAroundPoint(loc, 1.1 + radius)) {
				if(!WaterAbility.isWaterbendable(b.getType()) && !GeneralMethods.isRegionProtectedFromBuild(this, loc)) {
					if(b.getLocation().getBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
						b.getLocation().getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
					}
				}
			}
		}
		
		int upper = 12;
		Random random = new Random();
		int rand = random.nextInt(upper);
		if(rand == 1) {
			FireAbility.playFirebendingSound(loc);
		}
	}
	
	private void newFlameEffect() {
		phi += Math.PI / 20;
		for(double theta = 0; theta < 0.002*Math.PI * radiusF * phi; theta += Math.PI / 160) {
			double r = radiusF;
			double X = r*Math.sin(theta)*Math.sin(phi);
			double Y = r*Math.cos(phi);
			double Z = r*Math.cos(theta)*Math.sin(phi);
			effectLocation.add(X,Y,Z);
			ParticleEffect.FLAME.display(effectLocation, 0, 0, 0, 0.02f, 1);
			effectLocation.subtract(X,Y,Z);;
		}
		for(double theta = 0; theta < 0.002*Math.PI * radiusF * phi; theta += Math.PI / 20) {
			double r = radiusF;
			double X = r*Math.sin(theta)*Math.sin(phi);
			double Y = r*Math.cos(phi);
			double Z = r*Math.cos(theta)*Math.sin(phi);
			effectLocation.add(X,Y,Z);
			ParticleEffect.SMOKE_LARGE.display(effectLocation, 0, 0, 0, 0.02f, 1);
			effectLocation.subtract(X,Y,Z);;
		}
		
		// reverse
		for(double theta = 0; theta < 0.002*Math.PI * radiusF * phi; theta += Math.PI / 160) {
			double r = radiusF;
			double X = r*Math.sin(theta)*Math.sin(phi);
			double Y = r*Math.cos(phi);
			double Z = r*Math.cos(theta)*Math.sin(phi);
			effectLocation2.subtract(X,Y,Z);
			ParticleEffect.FLAME.display(effectLocation2, 0, 0, 0, 0.02f, 1);
			effectLocation2.add(X,Y,Z);
		}
		for(double theta = 0; theta < 0.002*Math.PI * radiusF * phi; theta += Math.PI / 20) {
			double r = radiusF;
			double X = r*Math.sin(theta)*Math.sin(phi);
			double Y = r*Math.cos(phi);
			double Z = r*Math.cos(theta)*Math.sin(phi);
			effectLocation2.subtract(X,Y,Z);
			ParticleEffect.SMOKE_LARGE.display(effectLocation2, 0, 0, 0, 0.02f, 1);
			effectLocation2.add(X,Y,Z);
		}
		
	}
	
	private void newFlameEffect2() {
		t = t + Math.PI/24;
		double x = radiusF*Math.cos(t);
		double y = Math.sin(t);
		double z = radiusF*Math.sin(t);
		effectLocation.add(x,y,z);
		ParticleEffect.FLAME.display(effectLocation, 0, 0, 0, 0.005f, 5);
		ParticleEffect.SMOKE_LARGE.display(effectLocation, 0, 0, 0, 0.002f, 1);
		effectLocation.subtract(x,y,z);

		double x2 = radiusF*Math.cos(t);
		double y2 = Math.cos(t);
		double z2 = radiusF*Math.sin(t);
		effectLocation2.subtract(x2,y2,z2);
		ParticleEffect.FLAME.display(effectLocation2, 0, 0, 0, 0.005f, 5);
		ParticleEffect.SMOKE_LARGE.display(effectLocation2, 0, 0, 0, 0.002f, 1);
		effectLocation2.add(x2,y2,z2);
		
		//reverse
		double x3 = radiusF*Math.cos(t) * Math.cos(t);
		double y3 = Math.sin(t);
		double z3 = radiusF*Math.sin(t) * Math.cos(t);
		effectLocation3.subtract(x3,y3,z3);
		ParticleEffect.FLAME.display(effectLocation3, 0, 0, 0, 0.005f, 5);
		effectLocation3.add(x3,y3,z3);
		
		double x4 = radiusF*Math.cos(t) * Math.sin(t);
		double y4 = Math.cos(t);
		double z4 = radiusF*Math.sin(t) * Math.sin(t);
		effectLocation4.add(x4,y4,z4);
		ParticleEffect.FLAME.display(effectLocation4, 0, 0, 0, 0.005f, 5);
		effectLocation4.subtract(x4,y4,z4);
		if(t > 24) {
			t = 0;
		}
	}
	
	@Override
	public String getDescription() {
		return config.get().getString("Combos.Fire.FlameRush.Description");
	}

	@Override
	public String getAuthor() {
		return "Soringaming";
	}

	@Override
	public String getVersion() {
		return "v2.0";
	}

	@Override
	public void load() {
		config = new Config(new File("Sorin.yml"));

		FileConfiguration c = config.get();
		
		c.addDefault("Combos.Fire.FlameRush.Description", "This combo creates an area of effect fire wave that will travel in the direction you're looking.");
		c.addDefault("Combos.Fire.FlameRush.Damage", 8);
		c.addDefault("Combos.Fire.FlameRush.Cooldown", 10000);
		c.addDefault("Combos.Fire.FlameRush.Range", 25);
		c.addDefault("Combos.Fire.FlameRush.Speed", 1.3);
		c.addDefault("Combos.Fire.FlameRush.CreateFlames", true);
		
		config.save();
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.flamerush") == null) {
			Permission perm = new Permission("bending.ability.flamerush");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new FireShotListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Successfully enabled " + getName() + " " + getVersion() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " " + getVersion() + " by " + getAuthor());
		super.remove();
	}

}
