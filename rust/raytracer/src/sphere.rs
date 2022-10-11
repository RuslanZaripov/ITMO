use crate::{Equation, Vec3};
use crate::hittable::{HitRecord, Hittable};
use crate::material::Material;
use crate::ray::Ray;
use crate::vec3::dot;

#[derive(Debug, Copy, Clone)]
pub struct Sphere<M: Material> {
    pub center: Vec3,
    pub radius: f64,
    pub material: M,
}

impl<M: Material> Hittable for Sphere<M> {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let oc = ray.origin - self.center;
        let eq = Equation::new(
            dot(&ray.dir, &ray.dir),
            2.0 * dot(&oc, &ray.dir),
            dot(&oc, &oc) - self.radius * self.radius,
        );
        return match eq.solve() {
            Some((t1, t2)) => {
                match in_bounds(t_min, t_max, t1, t2) {
                    Some(t) => self.helper(ray, t),
                    None => None
                }
            }
            None => None
        }
        // let (t1, t2) =
        //     if let Some((t1, t2)) = eq.solve() {
        //         (t1, t2)
        //     } else {
        //         return None;
        //     };
        // let mut root = t1;
        // if root < t_min || t_max < root {
        //     root = t2;
        //     if root < t_min || t_max < root {
        //         return None;
        //     }
        // }
        // let r = ray.at(root);
        // let outward_normal = (r - self.center) / self.radius;
        // let mut rec = HitRecord::new(root, r, outward_normal, false, &self.material);
        // rec.set_face_normal(ray, &outward_normal);
        // return Some(rec);
    }
}

fn in_bounds(t_min: f64, t_max: f64, t1: f64, t2: f64) -> Option<f64> {
    if t_min < t1  && t1 < t_max {
        Some(t1)
    } else if t_min < t2 && t2 < t_max {
        Some(t2)
    } else {
        None
    }
}

impl<M: Material> Sphere<M> {
    fn helper(&self, ray: &Ray, root: f64) -> Option<HitRecord> {
        let p = ray.at(root);
        let outward_normal = (p - self.center) / self.radius;
        let mut rec = HitRecord::new(root, p, outward_normal, &self.material, false);
        rec.set_face_normal(ray, &outward_normal);
        return Some(rec);
    }
}

impl<M: Material> Sphere<M> {
    pub fn new(center: Vec3, radius: f64, material: M) -> Self {
        Self { center, radius, material }
    }
}