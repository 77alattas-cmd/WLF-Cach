with open("app/src/main/java/com/example/ui/TicketAccountingApp.kt", "r") as f:
    c = f.read()
# Let's fix line 289
if "}" in c[-10:]:
    pass
# we can just fix by looking at the diffs. Let's see what's wrong with line 289
