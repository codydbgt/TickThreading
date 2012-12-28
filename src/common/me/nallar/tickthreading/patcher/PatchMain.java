package me.nallar.tickthreading.patcher;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import me.nallar.tickthreading.Log;
import me.nallar.tickthreading.mappings.MCPMappings;
import me.nallar.tickthreading.mappings.Mappings;
import me.nallar.tickthreading.util.CollectionsUtil;
import org.xml.sax.SAXException;

public class PatchMain {
	public static void main(String[] argv) {
		if (argv.length < 1) {
			Log.severe("Type must be passed");
			return;
		}
		String type = argv[0];
		String[] args = Arrays.copyOfRange(argv, 1, argv.length);
		if (type.equalsIgnoreCase("obfuscator")) {
			obfuscator(args);
		} else if (type.equalsIgnoreCase("patcher")) {
			patcher(args);
		} else {
			Log.severe(type + " is not a valid action.");
		}
	}

	public static void obfuscator(String[] args) {
		String mcpConfigPath = args.length > 0 ? args[0] : "build/forge/mcp/conf";
		String inputPatchPath = args.length > 1 ? args[1] : "resources/patches-unobfuscated.xml";
		String outputPatchPath = args.length > 2 ? args[2] : "build/classes/patches.xml";

		Log.info("Obfuscating " + inputPatchPath + " to " + outputPatchPath + " via " + mcpConfigPath);

		try {
			Mappings mcpMappings = new MCPMappings(new File(mcpConfigPath));
			PatchManager patchManager = new PatchManager(new File(inputPatchPath).toURI().toURL().openStream(), Patches.class);

			try {
				patchManager.obfuscate(mcpMappings);
				patchManager.save(new File(outputPatchPath));
			} catch (TransformerException e) {
				Log.severe("Failed to save obfuscated patch");
			}
		} catch (IOException e) {
			Log.severe("Failed to read input file", e);
		} catch (SAXException e) {
			Log.severe("Failed to parse input file", e);
		}
	}

	public static void patcher(String[] args) {
		boolean forcePatching = args.length >= 2 && args[1].equalsIgnoreCase("force");
		PatchManager patchManager;
		try {
			patchManager = new PatchManager(PatchMain.class.getResourceAsStream("/patches.xml"), Patches.class);
		} catch (Exception e) {
			Log.severe("Failed to initialize Patch Manager", e);
			return;
		}
		try {
			Iterable<File> filesToLoad = (Iterable<File>) CollectionsUtil.toObjects(CollectionsUtil.split(args[0]), File.class);
			patchManager.classRegistry.forcePatching = forcePatching;
			patchManager.loadBackups(filesToLoad);
			patchManager.classRegistry.loadFiles(filesToLoad);
			patchManager.runPatches();
		} catch (IOException e) {
			Log.severe("Failed to load jars", e);
		} catch (Exception e) {
			Log.severe("Unhandled patching failure", e);
		}
		try {
			Log.info("Done. Press enter to exit.");
			System.console().readLine();
		} catch (Exception ignored) {
		}
	}
}