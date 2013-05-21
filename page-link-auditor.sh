# Audits links recursively from a seed URL following links to a given depth,
# to check that a page and its descendants work as well in a new domain as in an old one.
#
# Args:
# 1. Old host of the page (eg "www.gulocal.co.uk")
# 2. New host of the page (eg "www.thegulocal.com")
# 3. Relative path of the page (eg "/music/2013/jan/20")
# 4. Depth of links to follow
# 5. Optional proxy (eg "ec2-xyz.com:8080")
# 6. Optional content of page to search for and report on (eg "some text")

oldHost=$1
newHost=$2
seedPath=$3
depth=$4
proxy=$5
contentToFind=$6

now=$(date +%FT%H-%M)

eval "sbt -Dsbt.log.noformat=true 'run-main com.gu.LinkAuditor.PageSpiderClient $oldHost $newHost $seedPath $depth \"$proxy\" \"$contentToFind\"'" \
    > audit.out.$now.log \
    2> audit.err.$now.log
