# Minecraft Code Breaking Changes
### 1.19.4
Current master

### 1.20.3
- `executeWithPrefix` had its return value removed. (No code changes required, but needs recompilation)

### 1.20.5
- Book content is now a DataComponent.

### 1.21.2
- `TypedActionResult` was removed in favor of `ActionResult`
- `getCommandSource()` was moved to `ServerPlayerEntity`.

### 1.21.5
- The unmapped name of `ItemStack.get` changed. No code change required, but needs recompilation.
