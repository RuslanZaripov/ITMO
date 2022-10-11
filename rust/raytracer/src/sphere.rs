use crate::{Equation, Vec3};
use crate::hittable::{HitRecord, Hittable};
use crate::ray::Ray;
use crate::vec3::dot;

#[derive(Debug, Copy, Clone)]
pub struct Sphere {
    pub center: Vec3,
    pub r: f64,
}

impl Hittable for Sphere {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let oc = ray.origin - self.center;
        let eq = Equation::new(
            dot(&ray.dir, &ray.dir),
            2.0 * dot(&oc, &ray.dir),
            dot(&oc, &oc) - self.r * self.r,
        );
        let (t1, t2) =
            if let Some((t1, t2)) = eq.solve() {
                (t1, t2)
            } else {
                return None;
            };
        let mut root = t1;
        if root < t_min || t_max < root {
            root = t2;
            if root < t_min || t_max < root {
                return None;
            }
        }
        let r = ray.at(root);
        let outward_normal = (r - self.center) / self.r;
        let mut rec = HitRecord::new(root, r, outward_normal, false);
        rec.set_face_normal(ray, &outward_normal);
        return Some(rec);

    }
}

impl Sphere {
    pub fn new(center: Vec3, radius: f64) -> Sphere {
        Sphere { center, r: radius }
    }
}