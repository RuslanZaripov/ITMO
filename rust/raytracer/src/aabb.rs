use crate::{Ray, Vec3};

pub struct AABB {
    pub min: Vec3,
    pub max: Vec3,
}

impl AABB {
    pub fn new(min: Vec3, max: Vec3) -> Self {
        Self { min, max }
    }

    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> bool {
        let t0 = (self.min - ray.origin) / ray.dir;
        let t1 = (self.max - ray.origin) / ray.dir;
        for i in 0..3 {
            let t_min = f64::max(f64::min(t0.at(i), t1.at(i)), t_min);
            let t_max = f64::min(f64::max(t0.at(i), t1.at(i)), t_max);
            if t_max <= t_min {
                return false;
            }
        }
        true
    }
}

pub fn surrounding_box(first_box: &AABB, second_box: &AABB) -> AABB {
    let small = Vec3::new(
        f64::min(first_box.min.x, second_box.min.x),
        f64::min(first_box.min.y, second_box.min.y),
        f64::min(first_box.min.z, second_box.min.z),
    );
    let big = Vec3::new(
        f64::max(first_box.max.x, second_box.max.x),
        f64::max(first_box.max.y, second_box.max.y),
        f64::max(first_box.max.z, second_box.max.z),
    );
    AABB::new(small, big)
}