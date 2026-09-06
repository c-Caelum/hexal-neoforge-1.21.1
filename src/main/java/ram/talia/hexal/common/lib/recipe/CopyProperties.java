package ram.talia.hexal.common.lib.recipe;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class CopyProperties {
    //Copied from https://github.com/FallingColors/Hexal/blob/main/Common/src/main/java/ram/talia/hexal/common/recipe/CopyProperties.java
    //A person making this commit does wholeheartedly blame kotlin

	// Because kotlin doesn't like doing raw, unchecked types
	// Can't blame it, but that's what we need to do
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static BlockState copyProperties(BlockState original, BlockState copyTo) {
		for (Property prop : original.getProperties()) {
			if (copyTo.hasProperty(prop)) {
				copyTo = copyTo.setValue(prop, original.getValue(prop));
			}
		}
		
		return copyTo;
	}
}
