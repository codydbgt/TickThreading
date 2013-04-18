package me.nallar.patched.storage;

import me.nallar.tickthreading.patcher.Declare;
import net.minecraft.world.chunk.NibbleArray;

public abstract class PatchNibbleArray extends NibbleArray {
	public PatchNibbleArray(final int par1, final int par2) {
		super(par1, par2);
	}

	@Override
	@Declare
	public Object copy() {
		byte[] newData = data == null ? null : data.clone();
		return new NibbleArray(newData, this.depthBits);
	}
}
