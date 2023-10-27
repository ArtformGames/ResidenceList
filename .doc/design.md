## Residence Data

Stored at `<DATA>/residences/{residence-name}.yml`.


```yaml
# MATERIAL TYPE THAT DISPLAYED IN GUI
icon: "NETHER_STAR"

# THE REGION NAME
name: "Display name"

# THE REGION DESCRIPTION
description:
  - "A nice residence region"

# WHETHER  THIS REGION SHOULD BE DISPLAYED IN PUBLIC GUI
public: false

# USER RATES TO THIS RESIDENCE
rates:
  "{user-uuid}":
    time: "2023-05-01 21:44:25"
    recommend: true
    content:
      - "A good region that very beautiful."
      - "I like it."

# BLOCKED USERS WHO CANNOT COMMENT AND SEE THIS REGION
blocked:
  - "{user-uuid}"
```

## User Data

Stored at `<DATA>/users/{user-uuid}.yml`.

```yaml
pined:
  - "A-Res"
  - "B-Res"
```